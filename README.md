# Spring Batch Fixed Width Flat File Sample

A Sample batch application that reads a flat file with different record types, stores then 
into objects and prints then to the console.

This app is built in kotlin with [spring batch framework](https://spring.io/projects/spring-batch).

## Running the app

Run the app by executing the following command from inside the project folder:

```shell
./gradlew bootRun
```

The app will run and look for a file named `sample-file.cred` in the classpath (a sample 
one is provided in the resources' directory).
The file will be processed and the result will be printed in the console.

## File Specs

The file can have one or more batches and each batch may start with a header record
and end with a trailer record. Each record is composed of 81 characters.

The batch contents are composed with n registration records and n credit records.
Each record specification is described below.

### Header

| Begin | End | Size | Name          | Type       | Description                                    |
|-------|-----|------|---------------|------------|------------------------------------------------|
| 1     | 2   | 2    | CD-REC-TYPE   | Fixed "01" | Type of the record.                            |
| 3     | 5   | 3    | NU-BATCH      | Number     | The sequential number of the batch in the file |
| 6     | 13  | 8    | DT-GENERATION | Date       | The date of generation of this batch           |
| 14    | 81  | 68   | FILLER        | FILLER     | A filler to fill the remaining space           |

Example:

```
0100120230129                                                                   |
```

### Client Registration

| Begin | End | Size | Name            | Type       | Description                                              |
|-------|-----|------|-----------------|------------|----------------------------------------------------------|
| 1     | 2   | 2    | CD-RET-TYPE     | Fixed "02" | Type of the record.                                      |
| 3     | 16  | 14   | CD-CLIENT       | Text       | The client code.                                         |
| 17    | 66  | 50   | TX-CLIENT       | Text       | The client name.                                         |
| 67    | 68  | 2    | CD-CLIENT-TYPE  | Number     | The client type.                                         |
| 69    | 70  | 2    | CD-PERSON-TYPE  | Text       | The client person type. PF for person, PJ for companies. |
| 71    | 80  | 2    | CD-PAYMENT-TYPE | Text       | The payment type. (AVULSO, MENSAL)                       |
| 81    | 81  | 1    | FILLER          | FILLER     | A filler to fill the remaining space                     |

Example: 

```
0200082429748967Fulano Ciclano                                    01PFAVULSO    |
```

### Credit

| Begin | End | Size | Name            | Type         | Description                               |
|-------|-----|------|-----------------|--------------|-------------------------------------------|
| 1     | 2   | 2    | CD-RET-TYPE     | Fixed "03"   | Type of the record.                       |
| 3     | 16  | 14   | CD-CLIENT       | Text         | The client code.                          |
| 17    | 24  | 8    | DT-CREDIT       | Date         | The credit payment date.                  |
| 25    | 31  | 7    | NU-VALUE        | Number (5,2) | The credit value with 2 decimal digits.   |
| 32    | 81  | 50   | FILLER          | FILLER       | A filler to fill the remaining space      |

Example:

```
0300082429748967202302100198000                                                 |
```

### Trailer

| Begin | End | Size | Name            | Type       | Description                                   |
|-------|-----|------|-----------------|------------|-----------------------------------------------|
| 1     | 2   | 2    | CD-RET-TYPE     | Fixed "04" | Type of the record.                           |
| 3     | 5   | 3    | QT-REGISTRATION | Number     | Quantity of registration records in the batch |
| 6     | 8   | 3    | QT-CREDIT       | Number     | Quantity of credit records in the batch       |
| 9     | 81  | 83   | FILLER          | FILLER     | A filler to fill the remaining space          |

Example:

```
04003005                                                                        |
```

# Copyright Notice

Copyright (c) 2023 Global Byte - Fábio M. Blanco

## License

Released under [MIT Lisense](https://github.com/fabio-blanco/spring-batch-fixed-width-flat-file-sample/blob/main/LICENSE).
