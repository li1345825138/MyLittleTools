# MyLittleTools

A collection of useful small tools written in Java for various everyday tasks including file hashing, image conversion, PDF manipulation, and password generation.

## Features

- File hash comparison (SHA-256)
- WEBP to JPG image conversion
- JPG images to PDF converter
- PDF merging utility
- Multiple JPG images merging into one
- Random password generator with history tracking
- SQLite database viewer for password history
- Extract images from PDF (planned feature)

## Tech Stack

- Java 21
- Maven
- Apache PDFBox 3.0.0
- SQLite JDBC 3.47.0.0
- TwelveMonkeys ImageIO WEBP plugin 3.9.4

## Building the Project

To build the project, you need to have Java 21 and Maven installed on your system.

```bash
mvn clean package
```

This will generate a fat JAR file in the `target` directory named `MyLittleTools-jar-with-dependencies.jar`.

## Usage

After building the project, you can run the tool with the following command:

```bash
java -jar MyLittleTools-jar-with-dependencies.jar [option] [arguments...]
```

### Available Options

#### File Hash Comparison
Compare SHA-256 hashes of two files to check if they are identical.
```bash
java -jar MyLittleTools-jar-with-dependencies.jar -hash file1 file2
```

#### WEBP to JPG Conversion
Convert all WEBP images in a directory to JPG format.
```bash
java -jar MyLittleTools-jar-with-dependencies.jar -w imageFolderPath
```

#### JPG to PDF Converter
Combine multiple JPG images from a directory into a single PDF file.
```bash
java -jar MyLittleTools-jar-with-dependencies.jar -p imageDirectory finalSaveName.pdf
```

#### PDF Merger
Merge multiple PDF files from a directory into a single PDF.
```bash
java -jar MyLittleTools-jar-with-dependencies.jar -m pdfDirectory finalSaveName.pdf
```

#### Multiple JPG Images Merger
Merge multiple JPG images vertically into a single JPG image.
```bash
java -jar MyLittleTools-jar-with-dependencies.jar -mi jpgDirectory finalSaveName.jpg
```

#### Random Password Generator
Generate a random password with specified length (minimum 8, maximum 1024).
Passwords are stored in a local SQLite database to ensure uniqueness.
```bash
java -jar MyLittleTools-jar-with-dependencies.jar -randpass length
```

#### SQLite Database Viewer
View all previously generated passwords stored in the SQLite database.
```bash
java -jar MyLittleTools-jar-with-dependencies.jar -viewsql databasePath
```

#### Extract Images from PDF (Not Implemented)
This feature is planned but not yet implemented.
```bash
java -jar MyLittleTools-jar-with-dependencies.jar -extractImages pdfFileName targetDirectory
```

#### Help
Display this help message.
```bash
java -jar MyLittleTools-jar-with-dependencies.jar -h
```

## Notes

- For image merging operations, files are processed in alphabetical order
- The random password generator creates passwords with alphanumeric characters and special symbols (!#$@)
- Generated passwords are automatically stored in a SQLite database named `database.sql` in the current directory to prevent duplicates
- All operations provide progress feedback in the console
- The "extract images from PDF" feature is planned but not yet implemented