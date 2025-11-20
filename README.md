# intermunifast

## Environment Variables

You can set the following environment variables in a `.env` file at the root of the project:

Or dont do it and default values are used in application.properties

```
DB_HOST=your_database_host
DB_PORT=your_database_port
DB_NAME=your_database_name
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password
```

## Run it

### Easiest

```
docker compose up -d
```

### Linux

```
docker run --name postgres-reservabus -e POSTGRES_PASSWORD=test_password -e POSTGRES_USER=test_user -e POSTGRES_DB=reservabus_test -p 5432:5432 -d postgres:16-alpine

cd reservaBus/reservaBus && ./mvnw clean spring-boot:run -DskipTests

cd intermunifast-web && npm install && npm run dev
```

And seed the database with

```
chmod +x seed.sh
./seed.sh
```

### Windows

```
docker run --name postgres-reservabus -e POSTGRES_PASSWORD=test_password -e POSTGRES_USER=test_user -e POSTGRES_DB=reservabus_test -p 5432:5432 -d postgres:16-alpine

cd reservaBus\reservaBus && mvnw.cmd clean spring-boot:run -DskipTests
cd intermunifast-web && npm install && npm run dev
```

And seed the database with

```
.\seed.ps1
```
