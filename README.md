# PROYECTOFINAL

Aplicación web de gestión de trámites estudiantiles de la UTEQ

## Proceso de ejecución

Se requiere JAVA JDK 21 instalado.

Abrir una terminal en la carpeta `Backend` y ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

---

## Endpoints

### 1. Authentication

- **POST** `http://localhost:8080/api/v1/auth/register`
  ```json
  {
    "names": "Juan",
    "surnames": "Perez",
    "cardId": "1203334445",
    "institutionalEmail": "juan.perez@uteq.edu.ec",
    "personalMail": "juan@gmail.com",
    "phoneNumber": "0998887776",
    "password": "mypassword123"
  }
  ```
- **POST** `http://localhost:8080/api/v1/auth/authenticate`
  ```json
  {
    "email": "juan.perez@uteq.edu.ec",
    "password": "mypassword123"
  }
  ```
- **POST** `http://localhost:8080/api/v1/auth/refresh-token`
  ```json
  {
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cAI..."
  }
  ```

### 2. Users

- **GET** `http://localhost:8080/api/v1/users`
- **POST** `http://localhost:8080/api/v1/users`
  ```json
  {
    "names": "Carlos",
    "surnames": "Decano",
    "cardId": "0991112223",
    "institutionalEmail": "decano@uteq.edu.ec",
    "personalMail": "decano@gmail.com",
    "phoneNumber": "0991234567",
    "password": "securepass",
    "roleName": "DEAN"
  }
  ```
- **PUT** `http://localhost:8080/api/v1/users`
  ```json
  {
    "idUser": 1,
    "names": "Carlos Modificado",
    "surnames": "Decano",
    "cardId": "0991112223",
    "institutionalEmail": "decano@uteq.edu.ec",
    "personalMail": "decano@gmail.com",
    "phoneNumber": "0991234567"
  }
  ```

### 3. Academic Calendar

- **GET** `http://localhost:8080/api/v1/academic-calendar`
- **POST** `http://localhost:8080/api/v1/academic-calendar`
  ```json
  {
    "calendarname": "Calendario 2024-2025 Ciclo I",
    "academicperiod": "2024-2025-I",
    "startdate": "2024-05-01",
    "enddate": "2024-09-30",
    "active": true
  }
  ```
- **PUT** `http://localhost:8080/api/v1/academic-calendar/{id}`
- **DELETE** `http://localhost:8080/api/v1/academic-calendar/{id}`

### 4. Careers

- **GET** `http://localhost:8080/api/v1/careers`
- **POST** `http://localhost:8080/api/v1/careers`
  ```json
  {
    "careername": "Ingeniería en Software",
    "careercode": "SOFT-2024",
    "facultiesidfaculty": 1,
    "coordinatoriduser": 10
  }
  ```
- **PUT** `http://localhost:8080/api/v1/careers/{id}`
- **DELETE** `http://localhost:8080/api/v1/careers/{id}`

### 5. Faculty

- **GET** `http://localhost:8080/api/v1/faculty`
- **POST** `http://localhost:8080/api/v1/faculty`
  ```json
  {
    "facultyname": "Facultad de Ciencias de la Ingeniería",
    "facultycode": "FCI",
    "deaniduser": 5
  }
  ```
- **PUT** `http://localhost:8080/api/v1/faculty/{id}`
- **DELETE** `http://localhost:8080/api/v1/faculty/{id}`

### 6. Configuration

- **GET** `http://localhost:8080/api/v1/configuration`
- **POST** `http://localhost:8080/api/v1/configuration`
  ```json
  {
    "profilepicturepath": "/uploads/profile.jpg",
    "signaturepath": "/uploads/signature.png",
    "enable_sms": true,
    "enable_email": true,
    "enable_whatsapp": false,
    "notificationfrequency": "INSTANT"
  }
  ```
- **PUT** `http://localhost:8080/api/v1/configuration/{id}`
- **DELETE** `http://localhost:8080/api/v1/configuration/{id}`

### 7. Deadline Rules

- **GET** `http://localhost:8080/api/v1/deadlinerules`
- **POST** `http://localhost:8080/api/v1/deadlinerules`
  ```json
  {
    "rulename": "Regla Peticiones Estándar",
    "procedurecategory": "ACADEMICO",
    "basedeadlinedays": 15,
    "warningdaysbefore": 3,
    "active": true
  }
  ```
- **PUT** `http://localhost:8080/api/v1/deadlinerules/{id}`
- **DELETE** `http://localhost:8080/api/v1/deadlinerules/{id}`

### 8. Document Templates

- **GET** `http://localhost:8080/api/v1/document-templates`
- **POST** `http://localhost:8080/api/v1/document-templates`
  ```json
  {
    "templatename": "Certificado de Matrícula",
    "templatecode": "CERT-MAT-001",
    "templatepath": "/templates/cert_mat.docx",
    "documenttype": "CERTIFICADO",
    "version": "1.0",
    "requiressignature": true,
    "active": true
  }
  ```
- **PUT** `http://localhost:8080/api/v1/document-templates/{id}`
- **DELETE** `http://localhost:8080/api/v1/document-templates/{id}`

### 9. Processing Stages

- **GET** `http://localhost:8080/api/v1/processing-stages`
- **POST** `http://localhost:8080/api/v1/processing-stages`
  ```json
  {
    "stagename": "Revisión de Secretaría",
    "stagecode": "REV-SEC",
    "stagedescription": "Revisión inicial de documentos",
    "stageorder": 1,
    "requiresapproval": true,
    "maxdurationdays": 2
  }
  ```
- **PUT** `http://localhost:8080/api/v1/processing-stages/{id}`
- **DELETE** `http://localhost:8080/api/v1/processing-stages/{id}`

### 10. Workflows

- **GET** `http://localhost:8080/api/v1/work-flows`
- **POST** `http://localhost:8080/api/v1/work-flows`
  ```json
  {
    "workflowname": "Flujo de Aprobación de Tesis",
    "workflowdescription": "Pasos para aprobar tema de tesis",
    "active": true
  }
  ```
- **PUT** `http://localhost:8080/api/v1/work-flows/{id}`
- **DELETE** `http://localhost:8080/api/v1/work-flows/{id}`

### 11. States

- **GET** `http://localhost:8080/api/v1/states`
- **POST** `http://localhost:8080/api/v1/states`
  ```json
  {
    "statename": "En Proceso",
    "statedescription": "El trámite está siendo revisado",
    "statecategory": "TRAMITE"
  }
  ```
- **PUT** `http://localhost:8080/api/v1/states/{id}`
- **DELETE** `http://localhost:8080/api/v1/states/{id}`

### 12. Reject Reasons

- **GET** `http://localhost:8080/api/v1/reject-reason`
- **POST** `http://localhost:8080/api/v1/reject-reason`
  ```json
  {
    "reasoncode": "DOC-INC",
    "reasondescription": "Documentación incompleta",
    "category": "DOCUMENTACION",
    "active": true
  }
  ```
- **PUT** `http://localhost:8080/api/v1/reject-reason/{id}`
- **DELETE** `http://localhost:8080/api/v1/reject-reason/{id}`

### 13. Permissions

- **GET** `http://localhost:8080/api/v1/permissions`
- **POST** `http://localhost:8080/api/v1/permissions`
  ```json
  {
    "code": "USR_READ",
    "description": "Permiso para leer usuarios"
  }
  ```
- **PUT** `http://localhost:8080/api/v1/permissions/{id}`
- **DELETE** `http://localhost:8080/api/v1/permissions/{id}`
