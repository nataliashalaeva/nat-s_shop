from flask import Flask, render_template, request, redirect, url_for, session, flash
from datetime import datetime, timezone
import matplotlib
matplotlib.use("Agg")
import firebase_admin
from firebase_admin import credentials, db
import os
import sys
from werkzeug.utils import secure_filename
from dotenv import load_dotenv
import plotly.express as px
from supabase import create_client
from collections import defaultdict
import logging
import time
import threading
import webbrowser
import traceback

load_dotenv()

SUPABASE_URL = os.getenv("SUPABASE_URL")
SUPABASE_KEY = os.getenv("SUPABASE_KEY")
SUPABASE_BUCKET = os.getenv("SUPABASE_BUCKET")
supabase = create_client(SUPABASE_URL, SUPABASE_KEY)

app = Flask(__name__)
app.secret_key = "supersecretkey"

def resource_path(relative_path):
    try:
        base_path = sys._MEIPASS
    except AttributeError:
        base_path = os.path.abspath(".")

    return os.path.join(base_path, relative_path)

cred = credentials.Certificate(resource_path("serviceAccountKey.json"))
firebase_admin.initialize_app(
    cred,
    {
        "databaseURL": "https://myshop-7f13a-default-rtdb.europe-west1.firebasedatabase.app/"
    },
)

headers = {"apikey": SUPABASE_KEY, "Authorization": f"Bearer {SUPABASE_KEY}"}

def format_datetime(timestamp, format="%d %B %Y"):
    if timestamp:
        # Используем fromtimestamp с временной зоной UTC
        return datetime.fromtimestamp(timestamp / 1000, timezone.utc).strftime(
            format
        )  # переводим из миллисекунд в секунды
    return "Не указано"

def format_timestamp(timestamp):
    if timestamp:
        # Если таймстемп в миллисекундах, разделим на 1000
        if len(str(timestamp)) > 10:
            timestamp = timestamp / 1000
        try:
            return datetime.fromtimestamp(timestamp).strftime("%Y-%m")
        except Exception as e:
            print(f"Ошибка преобразования timestamp: {e}")
    return "Не указано"
  
app.jinja_env.filters["datetime"] = format_datetime


@app.route("/", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        if request.form["username"] == "admin" and request.form["password"] == "admin":
            session["logged_in"] = True
            return redirect(url_for("dashboard"))
        else:
            flash("Неверный логин или пароль")
    return render_template("login.html")

# Добавление нового продукта (обработка POST-запроса)
@app.route("/add_product", methods=["POST"])
def add_product():
    try:
        name = request.form["name"]
        price = request.form["price"]
        image = request.files["image"]

        if not image:
            return "Изображение не загружено", 400

        if not os.path.exists(".venv/Scripts/temp"):
            os.makedirs("temp")

        filename = secure_filename(image.filename)
        temp_path = os.path.join(".venv/Scripts/temp", filename)
        image.save(temp_path)

        with open(temp_path, "rb") as f:
            response = supabase.storage.from_(SUPABASE_BUCKET).upload(
                f"images/{filename}", f
            )

        if hasattr(response, "error") and response.error:
            return f"Ошибка загрузки изображения: {response['error']['message']}", 500

        image_url = f"{SUPABASE_URL}/storage/v1/object/public/{SUPABASE_BUCKET}/images/{filename}"

        products_ref = db.reference("Products")
        all_products = products_ref.get()

        if not all_products:
            all_products = []

        new_index = (
            max([product["id"] for product in all_products if product], default=-1) + 1
        )

        product_data = {
            "id": new_index,
            "name": name,
            "price": price,
            "imageUrl": image_url,
        }

        # Добавляем или заменяем в нужной позиции
        products_ref.child(str(new_index)).set(product_data)

        return redirect("/dashboard")

    except Exception as e:
        traceback.print_exc()
        return f"Ошибка: {str(e)}", 500

# Удаление продукта по ID
@app.route("/delete_product/<product_id>", methods=["POST"])
def delete_product(product_id):
    try:
        product_id = int(product_id)
        products_ref = db.reference("Products")
        all_products = products_ref.get()

        if not isinstance(all_products, list):
            return redirect(url_for("dashboard"))

        if 0 <= product_id < len(all_products):
            all_products[product_id] = None

        new_products = []
        for item in all_products:
            if item:
                new_products.append(item)

        for i, prod in enumerate(new_products):
            prod["id"] = i

        products_ref.set(new_products)

    except Exception as e:
        traceback.print_exc()

    return redirect(url_for("dashboard"))
  
# Обновление статуса одного заказа
@app.route("/update_status/<order_id>", methods=["POST"])
def update_status(order_id):
    orders_data = db.reference("Orders").get() or {}
    new_status = request.form.get("status")

    if order_id in orders_data:
        db.reference(f"Orders/{order_id}/status").set(new_status)

        if new_status.lower() == "закрыт":
            order_data = db.reference(f"Orders/{order_id}").get()
            if order_data and not order_data.get("closedAt"):
                closed_at_timestamp = int(datetime.now().timestamp() * 1000)
                print(
                    f"Закрытие заказа {order_id}, дата (timestamp): {closed_at_timestamp}"
                )
                db.reference(f"Orders/{order_id}/closedAt").set(closed_at_timestamp)

    return redirect(url_for("dashboard", tab="orders"))

# Обновление данных продукта
@app.route("/update_product/<product_id>", methods=["POST"])
def update_product(product_id):
    try:
        name = request.form["name"]
        price = request.form["price"]
        image_file = request.files.get("image")

        products_ref = db.reference("Products")
        all_products = products_ref.get()

        if not all_products:
            return "Товар не найден", 404

        product_index = int(product_id)

        if product_index >= len(all_products) or all_products[product_index] is None:
            return "Товар не найден", 404

        product = all_products[product_index]
        product["name"] = name
        product["price"] = price

        if image_file and image_file.filename:
            filename = secure_filename(image_file.filename)
            temp_path = os.path.join(".venv/Scripts/temp", filename)
            image_file.save(temp_path)

            with open(temp_path, "rb") as f:
                supabase.storage.from_(SUPABASE_BUCKET).upload(f"images/{filename}", f)

            image_url = f"{SUPABASE_URL}/storage/v1/object/public/{SUPABASE_BUCKET}/images/{filename}"
            product["imageUrl"] = image_url

        # Обновляем весь список с новым продуктом
        all_products[product_index] = product
        products_ref.set(all_products)

        return redirect(url_for("dashboard"))

    except Exception as e:
        traceback.print_exc()
        return f"Ошибка: {str(e)}", 500


@app.route("/dashboard")
def dashboard():
    if not session.get("logged_in"):
        return redirect(url_for("login"))

    # Получаем продукты из Firebase
    raw_products = db.reference("Products").get() or []

    # Получаем заказы из Firebase
    orders_data = db.reference("Orders").get() or {}
    users = db.reference("Users").get() or {}
    if isinstance(raw_products, list):
        products_data = {str(i): p for i, p in enumerate(raw_products) if p}
    elif isinstance(raw_products, dict):
        products_data = raw_products
    else:
        products_data = {}

    total_products = len(products_data)
    prices = [float(p["price"]) for p in products_data.values() if p.get("price")]

    average_price = round(sum(prices) / len(prices), 2) if prices else 0
    max_price_product = max(
        products_data.values(), key=lambda x: float(x["price"]), default=None
    )
    min_price_product = min(
        products_data.values(), key=lambda x: float(x["price"]), default=None
    )

    deleted_products_count = 0
    if isinstance(raw_products, list):
        deleted_products_count = raw_products.count(None)

    analytics = {
        "total_products": total_products,
        "average_price": average_price,
        "most_expensive": max_price_product["name"] if max_price_product else "Нет",
        "cheapest": min_price_product["name"] if min_price_product else "Нет",
        "deleted_products": deleted_products_count,
    }

    price_graph = None
    if prices:
        price_fig = px.histogram(
            x=prices,
            nbins=20,
            title="Распределение цен товаров",
            labels={"x": "Цена товара", "y": "Количество"},
        )
        price_fig.update_layout(
            bargap=0.1,
            xaxis_title="Цена",
            yaxis_title="Количество товаров",
            yaxis=dict(
                tickmode="linear",  # Используем линейный режим для оси Y
                dtick=1,  # Шаг 1 для оси Y, чтобы были только целые числа
                tick0=0,  # Начало отсчета от 0
            ),
        )
        price_graph = price_fig.to_html(full_html=False)

    status_graph = None
    if orders_data:
        statuses = [order.get("status", "Неизвестно") for order in orders_data.values()]
        status_count = {status: statuses.count(status) for status in set(statuses)}

        status_fig = px.bar(
            x=list(status_count.keys()),
            y=list(status_count.values()),
            title="Распределение заказов по статусам",
            labels={"x": "Статус", "y": "Количество заказов"},
        )
        status_fig.update_layout(xaxis_title="Статус", yaxis_title="Количество закзов")
        status_graph = status_fig.to_html(full_html=False)

    monthly_orders = defaultdict(int)
    monthly_sales = defaultdict(float)

    for order in orders_data.values():
        if order.get("status") == "закрыт":
            closed_at = order.get("closedAt")
            if closed_at:
                month_key = format_timestamp(int(closed_at))

                if month_key != "Не указано":
                    product_list = order.get("productList", [])
                    items_count = len(product_list)
                    items_count = int(items_count)

                    if items_count > 0:
                        monthly_orders[month_key] += items_count
                    total_amount = order.get("totalAmount", 0)

                    if isinstance(total_amount, (int, float)):
                        monthly_sales[month_key] += float(total_amount)

    orders_graph = None
    if monthly_orders:
        months = sorted(list(set(monthly_orders.keys())))

        orders_fig = px.bar(
            x=months,
            y=[int(monthly_orders[month]) for month in months],
            title="Количество проданных товаров по месяцам",
            labels={"x": "Месяц", "y": "Количество товаров"},
        )

        orders_fig.update_layout(
            xaxis=dict(tickmode="array", tickvals=months, ticktext=months),
            xaxis_title="Месяц",
            yaxis_title="Количество товаров",
            yaxis=dict(range=[0, None]),
        )
        orders_graph = orders_fig.to_html(full_html=False)

    sales_data = [
        {"Месяц": month, "Сумма продаж": f" {monthly_sales[month]:,.2f}₽"}
        for month in monthly_sales
    ]

    return render_template(
        "dashboard.html",
        products=products_data,
        orders=orders_data,
        users=users,
        analytics=analytics,
        price_graph=price_graph,
        status_graph=status_graph,
        orders_graph=orders_graph,
        sales_data=sales_data,
    )

@app.route("/logout")
def logout():
    session.pop("logged_in", None)
    return redirect(url_for("login"))

if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    def open_browser():
        time.sleep(1)
        webbrowser.open("http://127.0.0.1:5000")

    threading.Thread(target=open_browser).start()
    app.run(debug=True, use_reloader=False)
