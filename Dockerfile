FROM java:8

COPY ./target/fastlane-0.9.0-rc4.jar /neolynks/fastlane.jar

ADD example.keystore /example.keystore

ADD example.yml /neolynks/example.yml

CMD java -jar /neolynks/fastlane.jar server /neolynks/example.yml

EXPOSE 8080 

