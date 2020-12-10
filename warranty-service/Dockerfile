FROM clojure
COPY . /usr/src/app
WORKDIR /usr/src/app
CMD java -jar ./target/uberjar/warranty-service-0.1.0-SNAPSHOT-standalone.jar 0.0.0.0 $PORT