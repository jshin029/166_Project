import os
from flask import redirect
from flask import Flask
from flask import render_template
from flask import request
from flask_sqlalchemy import SQLAlchemy
from dbinit import query
from sqlalchemy import create_engine
from sqlalchemy import case


app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'postgresql://localhost/johnshin_DB'
db = SQLAlchemy(app)

@app.route('/plane', methods=["GET", "POST"])
def plane():
    if request.form:
        try:
            make = request.form.get("make")
            model = request.form.get("model")
            age = request.form.get("age")
            seats = request.form.get("seats")

            query = "INSERT INTO Plane (id, make, model, age, seats) Values (nextval('plane_id_seq'), '"
            query += make
            query += "', '"
            query += model
            query += "', '"
            query += age
            query += "', '"
            query += seats
            query += "') RETURNING *;"
            result_set = db.engine.execute(query)
            result = result_set.fetchone()

            id = result[0]

            return render_template("plane.html", id=id, make=make, model=model, age=age, seats=seats)

        except Exception as e:
            print("Failed to add plane")
            print(e)
    return render_template("plane.html")

@app.route('/pilot', methods=["GET", "POST"])
def pilot():
    if request.form:
        try:
            full_name = request.form.get("full_name")
            nationality = request.form.get("nationality")

            query =  "INSERT INTO Pilot (id, fullname, nationality) Values (nextval('pilot_id_seq'), '"
            query += full_name
            query += "', '"
            query += nationality
            query += "') RETURNING *;"

            result_set = db.engine.execute(query)
            result = result_set.fetchone()
            id = result[0]

            return render_template("pilot.html", id=id, full_name=full_name, nationality=nationality)

        except Exception as e:
            print("Failed to add pilot")
            print(e)
    return render_template("pilot.html")

@app.route('/flight', methods=["GET", "POST"])
def flight():
    if request.form:
        try:
            cost = request.form.get("cost")
            num_sold = request.form.get("num_sold")
            num_stops = request.form.get("num_stops")
            actual_departure_date = request.form.get("actual_departure_date")
            actual_arrival_date = request.form.get("actual_arrival_date")
            arrival_airport = request.form.get("arrival_airport")
            departure_airport = request.form.get("departure_airport")

            query =  "INSERT INTO Flight (fnum, cost, num_sold, num_stops, actual_departure_date, actual_arrival_date, arrival_airport, departure_airport) Values (nextval('flight_id_seq'), '"
            query += cost
            query += "', '"
            query += num_sold
            query += "', '"
            query += num_stops
            query += "', '"
            query += actual_departure_date
            query += "', '"
            query += actual_arrival_date
            query += "', '"
            query += arrival_airport
            query += "', '"
            query += departure_airport
            query += "') RETURNING *;"

            result_set = db.engine.execute(query)
            result = result_set.fetchone()
            id = result[0]

            return render_template("flight.html", id=id, cost=cost, num_sold=num_sold, num_stops=num_stops, actual_departure_date=actual_departure_date, actual_arrival_date=actual_arrival_date, arrival_airport=arrival_airport, departure_airport=departure_airport)

        except Exception as e:
            print("Failed to add flight")
            print(e)
    return render_template("flight.html")

@app.route('/technician', methods=["GET", "POST"])
def technician():
    if request.form:
        try:
            full_name = request.form.get("full_name")

            query = "INSERT INTO Technician (id, full_name) Values (nextval('tech_id_seq'), '"

            query += full_name
            query += "') RETURNING *;"

            result_set = db.engine.execute(query)
            result = result_set.fetchone()
            id = result[0]

            return render_template("technician.html", id=id, full_name=full_name)

        except Exception as e:
            print("Failed to add technician")
            print(e)
    return render_template("technician.html")

@app.route('/bookflight', methods=["GET", "POST"])
def bookflight():
    if request.form:
        try:
            cid = request.form.get("cid")
            fid= request.form.get("fid")

            query = "SELECT (SELECT P.seats FROM Plane P WHERE P.id = (SELECT F.plane_id FROM FlightInfo F WHERE F.flight_id = "
            query2 = "(SELECT num_sold FROM Flight F WHERE F.fnum ="

            query += fid
            query += ")) - "
            query += query2
            query += fid
            query += ");"

            result_set = db.engine.execute(query)
            result = result_set.fetchone()
            difference = result[0]

            query_e = "INSERT INTO Reservation (rnum, cid, fid, status) Values(nextval('res_id_seq'), '"
            query_e += cid
            query_e += "', '"
            query_e += fid
            query_e += "', '"
            status = 'W'

            if difference > 0:
                status = 'C'
                query_e += 'C'
                query_e += "') RETURNING *;"
            else:
                query_e += 'W'
                query_e += "') RETURNING *;"

            result_set = db.engine.execute(query_e)
            result = result_set.fetchone()
            id = result[0]

            return render_template("bookflight.html", id=id, cid=cid, fid=fid, status=status)


            # query = "INSERT INTO Reservation (rnum, cid, fid, status) SELECT nextval('res_id_seq'), "
            # query += cid
            # query += ", "
            # query += fid
            # query += ", "
            #
            # query1 = "CASE WHEN (SELECT P.seats FROM Plane P WHERE P.id = (SELECT F.plane_id FROM FlightInfo F WHERE F.flight_id = "
            # query1_2 = ")) > (SELECT num_sold FROM Flight F WHERE F.fnum = "
            # query1_3 = ") THEN 'C' ELSE 'W' END;"
            #
            # query += query1
            # query += fid
            # query += query1_2
            # query += fid
            # query += query1_3
            #
            #
            # # query2 = "UPDATE Flight SET num_sold = num_sold+1  WHERE fnum = "
            # # query2 += fid
            # # query2 += ";"
            #
            # result_set = con.execute(query)
            # result = result_set.fetchone()
            # id = result[0]

            #db.engine.execute(query2)


        except Exception as e:
            print("Failed to book a flight")
            print(e)
    return render_template("bookflight.html")

@app.route('/listavailable', methods=["GET", "POST"])
def listavailable():
    if request.form:
        try:
            flightid = request.form.get("flightid")
            query = "SELECT (SELECT P.seats FROM Plane P WHERE P.id = (SELECT F.plane_id FROM FlightInfo F WHERE F.flight_id = "
            query2 = "(SELECT num_sold FROM Flight F WHERE F.fnum ="

            query += flightid
            query += ")) - "
            query += query2
            query += flightid
            query += ");"

            result_set = db.engine.execute(query)
            result = result_set.fetchone()
            difference = result[0]


            return render_template("listavailable.html", difference=difference)

        except Exception as e:
            print("Failed to list availability")
            print(e)
    return render_template("listavailable.html")

@app.route('/listrepairs', methods=["GET", "POST"])
def listrepairs():
    if request.method == "POST":
        try:
            query = "SELECT P.id, P.model, count(R.rid) FROM Plane P, Repairs R WHERE P.id = R.plane_id GROUP BY P.id ORDER BY count DESC;";
            result_set = db.engine.execute(query).fetchall()
            return render_template("listrepairs.html", result_set=result_set)

        except Exception as e:
            print("Failed to list number of repairs")
            print(e)
    return render_template("listrepairs.html")

@app.route('/listyear', methods=["GET", "POST"])
def listyear():
    if request.method == "POST":
        try:
            query = "SELECT EXTRACT (year FROM R.repair_date) as \"Year\", count(R.rid) FROM Repairs R GROUP BY \"Year\" ORDER BY count ASC;"
            result_set = db.engine.execute(query).fetchall()
            return render_template("listyear.html", result_set=result_set)

        except Exception as e:
            print("Failed to output list")
            print(e)
    return render_template("listyear.html")

@app.route('/countstatus', methods=["GET", "POST"])
def countstatus():
    if request.form:
        try:
            fnum= request.form.get("fnum")
            status = request.form.get("status")

            query = "SELECT COUNT(*) FROM Reservation R WHERE R.fid = "
            query += fnum
            query += "AND R.status = '"
            query += status
            query += "';"

            result_set = db.engine.execute(query).fetchone()
            result = result_set[0]

            return render_template("countstatus.html", fnum=fnum, status=status, result=result)

        except Exception as e:
            print("Failed to output list")
            print(e)
    return render_template("countstatus.html")



@app.route('/', methods=["GET", "POST"])
def main():
    return render_template("home.html")



if __name__ == "__main__":
    app.run()
