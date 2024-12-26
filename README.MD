# Spring Boot AI with OpenAI

## Description
- Simple application that presents power of AI with Spring Boot AI
- All files that has extension `st` are use in prompt that is sent to Open AI API, those file are stored in `resources/templates/prompts/`
- File `studijni-a-zkusebni-rad-univerzity-hradec-kralove-2021.pdf` needs to be converted to formated json
than `studijni-a-zkusebni-rad-univerzity-hradec-kralove-2021.json` and `movies500.csv` needs to be converted to Vector Store

## Used technology
- `Spring Boot AI` for communication with `Open AI API`
- For frontend is used Thymeleaf with Bootstrap

## TODO:
- Create new OPENAI TOKEN and remove old from Application.properties

## How to convert pdf to structured json
- Go to `src/main/java/com/pavelkostal/aiwithjava/helper/PdfToJsonConverter.java` and run this class it will convert
  `studijni-a-zkusebni-rad-univerzity-hradec-kralove-2021.pdf` to `studijni-a-zkusebni-rad-univerzity-hradec-kralove-2021.json`

## How it works:
- When application starts it will check if exist file `combined_vector_store.json` if it does not exist it will create
new vector from files `studijni-a-zkusebni-rad-univerzity-hradec-kralove-2021.json` and `movies500.csv`
- What file should be used is set in `application-properties` in `sfg.aiapp.documents-to-load`
- Where should be vector store saved is set in `application-properties` in `sfg.aiapp.vector-store-path`
- Vector store configuration is in `config` directory