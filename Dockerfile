# --- Stage 1: Build ---
# Sử dụng image Maven đi kèm với JDK 21 để biên dịch code
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Copy pom.xml và tải thư viện trước để tận dụng Docker Cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy thư mục src và tiến hành build file .jar
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Run ---
# Sử dụng JRE 21 nhẹ hơn để chạy ứng dụng (không cần bộ compiler của JDK)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy file jar từ stage build sang.
COPY --from=build /app/target/TravelEz.jar app.jar

# Copy file key Google Cloud từ máy thật vào container
COPY gcs-key.json /app/gcs-key.json

# Khai báo cổng ứng dụng
EXPOSE 8080

# Lệnh khởi chạy
ENTRYPOINT ["java", "-jar", "app.jar"]

