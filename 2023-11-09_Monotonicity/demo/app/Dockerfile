FROM rust:1.73.0-alpine3.18 AS builder
RUN apk add --no-cache musl-dev pkgconfig openssl-dev
COPY . /app 
WORKDIR /app

RUN cargo build --release --target=x86_64-unknown-linux-musl

FROM scratch
COPY --from=builder /app/target/x86_64-unknown-linux-musl/release/app /app/app
WORKDIR /app
ENTRYPOINT [ "/app/app" ]