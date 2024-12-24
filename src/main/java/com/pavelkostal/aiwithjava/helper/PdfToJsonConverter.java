package com.pavelkostal.aiwithjava.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * A utility class to convert PDF files to JSON format with metadata and structured content.
 */
public class PdfToJsonConverter {

    public static void main(String[] args) {
        String inputFilePath = Objects.requireNonNull(PdfToJsonConverter.class.getClassLoader()
                        .getResource("sourceData/studijni-a-zkusebni-rad-univerzity-hradec-kralove-2021.pdf"))
                        .getPath();

        Path resourceDirectory = Path.of("src/main/resources/sourceData/studijni-a-zkusebni-rad-univerzity-hradec-kralove-2021.json");
        String outputFilePath = resourceDirectory.toAbsolutePath().toString();

        try {
            // Extract text and metadata
            Map<String, Object> jsonOutput = new HashMap<>();

            // Step 1: Extract metadata and text using Apache Tika
            Metadata metadata = new Metadata();
            BodyContentHandler handler = new BodyContentHandler(-1); // No buffer size limit for large files
            AutoDetectParser parser = new AutoDetectParser();
            ParseContext context = new ParseContext();

            try (FileInputStream inputStream = new FileInputStream(inputFilePath)) {
                parser.parse(inputStream, handler, metadata, context);
            }

            String extractedText = handler.toString();
            jsonOutput.put("originalContent", extractedText); // Optional: Save full raw content

            // Step 2: Extract all available metadata
            Map<String, String> metadataMap = new HashMap<>();
            for (String name : metadata.names()) {
                metadataMap.put(name, metadata.get(name));
            }
            jsonOutput.put("metadata", metadataMap);

            // Step 3: Process and format structured content
            Map<String, Object> structuredContent = createStructuredContent(extractedText);
            jsonOutput.put("structuredContent", structuredContent);

            // Step 4: Write the JSON output to the specified file
            ObjectMapper objectMapper = new ObjectMapper();
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.UTF_8)) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, jsonOutput);
            }

            System.out.println("PDF successfully converted to JSON and saved to: " + outputFilePath);

        } catch (IOException | TikaException | org.xml.sax.SAXException e) {
            e.printStackTrace();
            System.err.println("An error occurred while processing the PDF: " + e.getMessage());
        }
    }

    /**
     * Processes raw extracted text to create a structured content map.
     * The content is divided into sections, subsections, etc., based on likely headings and markers.
     *
     * @param text The extracted raw text from the PDF.
     * @return A map representing the structured content.
     */
    private static Map<String, Object> createStructuredContent(String text) {
        Map<String, Object> sectionMap = new LinkedHashMap<>();

        // Split the text into lines for processing
        String[] lines = text.split("\n");
        String currentSection = null;
        StringBuilder currentContent = new StringBuilder();

        for (String line : lines) {
            // Detect section headings (e.g., "Čl. 1", "ČÁST PRVNÍ", etc.)
            if (line.matches("^[Čč]l\\..*|^ČÁST.*")) { // Example regex for headings
                if (currentSection != null) {
                    // Save the previous section
                    sectionMap.put(currentSection, currentContent.toString().trim());
                    currentContent.setLength(0); // Clear the buffer for new content
                }
                currentSection = line.trim(); // Start a new section
            } else {
                currentContent.append(line.trim()).append("\n");
            }
        }

        // Save the last section (if any)
        if (currentSection != null) {
            sectionMap.put(currentSection, currentContent.toString().trim());
        }

        return sectionMap;
    }
}