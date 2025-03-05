# Google Contacts API Integration

This Spring Boot application demonstrates OAuth 2.0 authentication with Google and provides REST endpoints to interact with Google Contacts API.

## Features

- OAuth 2.0 authentication with Google
- REST endpoints for Google Contacts operations:
  - Retrieve all contacts
  - Get a specific contact
  - Add a new contact
  - Update an existing contact
  - Delete a contact

## Prerequisites

- Java 17 or higher
- Maven
- Google Cloud Platform account with Google People API enabled

## Setup

### 1. Google Cloud Platform Setup

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google People API:
   - Navigate to "APIs & Services" > "Library"
   - Search for "Google People API" and enable it
4. Create OAuth 2.0 credentials:
   - Navigate to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "OAuth client ID"
   - Select "Web application" as the application type
   - Add "http://localhost:8080/login/oauth2/code/google" as an authorized redirect URI
   - Add "http://localhost:8080" as an authorized JavaScript origin
   - Note down the Client ID and Client Secret

### 2. Application Configuration

1. Clone this repository
2. Set the following environment variables:
   ```
   GOOGLE_CLIENT_ID=658873512848-7plt31a0nulps35doouo9qochummc9m3.apps.googleusercontent.com
   GOOGLE_CLIENT_SECRET=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
   ```

   Alternatively, you can update these values directly in `application.properties`.

## Running the Application

1. Build the application:
   ```
   mvn clean package
   ```

2. Run the application:
   ```
   java -jar target/MidtermActivity-0.0.1-SNAPSHOT.jar
   ```

3. Open a web browser and navigate to `http://localhost:8080`

## API Endpoints

Once authenticated, you can use the following endpoints:

### Get All Contacts
```
GET /api/contacts
```

### Get a Specific Contact
```
GET /api/contacts/{resourceName}
```

### Create a New Contact
```
POST /api/contacts
Content-Type: application/json

{
  "names": [
    {
      "givenName": "John",
      "familyName": "Doe",
      "displayName": "John Doe"
    }
  ],
  "emailAddresses": [
    {
      "value": "john.doe@example.com",
      "type": "work"
    }
  ],
  "phoneNumbers": [
    {
      "value": "+1234567890",
      "type": "mobile"
    }
  ]
}
```

### Update a Contact
```
PUT /api/contacts/{resourceName}
Content-Type: application/json

{
  "names": [
    {
      "givenName": "John",
      "familyName": "Smith",
      "displayName": "John Smith"
    }
  ],
  "emailAddresses": [
    {
      "value": "john.smith@example.com",
      "type": "work"
    }
  ],
  "phoneNumbers": [
    {
      "value": "+1234567890",
      "type": "mobile"
    }
  ]
}
```

### Delete a Contact
```
DELETE /api/contacts/{resourceName}
```

## Implementation Notes

- The application uses Spring WebClient to make REST calls to the Google People API
- OAuth 2.0 authentication is handled by Spring Security
- The application uses the following scopes: `openid`, `profile`, `email`, and `https://www.googleapis.com/auth/contacts`

## License

This project is licensed under the MIT License. 