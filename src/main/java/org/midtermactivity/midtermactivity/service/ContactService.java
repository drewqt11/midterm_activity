package org.midtermactivity.midtermactivity.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.midtermactivity.midtermactivity.model.Contact;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final WebClient googlePeopleClient;
    
    private static final String PERSON_FIELDS = "names,emailAddresses,phoneNumbers";
    
    /**
     * Retrieves all contacts from the authenticated user's Google Contacts.
     *
     * @return List of contacts
     */
    public List<Contact> getAllContacts() {
        try {
            Map<String, Object> response = googlePeopleClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/people/me/connections")
                            .queryParam("personFields", PERSON_FIELDS)
                            .queryParam("pageSize", 100)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response == null || !response.containsKey("connections")) {
                return Collections.emptyList();
            }
            
            List<Map<String, Object>> connections = (List<Map<String, Object>>) response.get("connections");
            return connections.stream()
                    .map(this::mapToContact)
                    .toList();
        } catch (Exception e) {
            log.error("Error retrieving contacts", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Retrieves a single contact by resource name.
     *
     * @param resourceName the resource name of the contact
     * @return the contact
     */
    public Contact getContact(String resourceName) {
        try {
            // Create a final variable for use in lambda
            final String finalResourceName;
            if (!resourceName.startsWith("people/")) {
                finalResourceName = "people/" + resourceName;
            } else {
                finalResourceName = resourceName;
            }

            return googlePeopleClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/" + finalResourceName)
                            .queryParam("personFields", PERSON_FIELDS)
                            .build())
                    .retrieve()
                    .bodyToMono(Contact.class)
                    .block();
        } catch (Exception e) {
            log.error("Error retrieving contact: {}", resourceName, e);
            return null;
        }
    }


    /**
     * Validates if a contact has required fields.
     * @param contact the contact to validate
     * @throws IllegalArgumentException if required fields are missing
     */
    private void validateContact(Contact contact) {
        if (contact.getNames() == null || contact.getNames().isEmpty()
                || contact.getNames().get(0).getGivenName() == null
                || contact.getNames().get(0).getGivenName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (contact.getEmailAddresses() == null || contact.getEmailAddresses().isEmpty()
                || contact.getEmailAddresses().get(0).getValue() == null
                || contact.getEmailAddresses().get(0).getValue().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (contact.getPhoneNumbers() == null || contact.getPhoneNumbers().isEmpty()
                || contact.getPhoneNumbers().get(0).getValue() == null
                || contact.getPhoneNumbers().get(0).getValue().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
    }
    /**
     * Creates a new contact.
     *
     * @param contact the contact to create
     * @return the created contact
     */
    public Contact createContact(Contact contact) {
        try {
            validateContact(contact);
            return googlePeopleClient.post()
                    .uri("/people:createContact")
                    .bodyValue(contact)
                    .retrieve()
                    .bodyToMono(Contact.class)
                    .block();
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating contact", e);
            return null;
        }
    }
    
    /**
     * Updates an existing contact.
     *
     * @param resourceName the resource name of the contact to update
     * @param contact the updated contact information
     * @return the updated contact
     */
    public Contact updateContact(String resourceName, Contact contact) {
        try {
            log.debug("Original resourceName received: {}", resourceName);
            
            // Simple resourceName normalization
            // Remove 'people/' prefix if it exists
            final String finalResourceName;
            if (resourceName.startsWith("people/")) {
                finalResourceName = resourceName.substring("people/".length());
            } else {
                finalResourceName = resourceName;
            }
            
            log.debug("Updating contact with normalized resourceName: {}", finalResourceName);
            log.debug("Contact data being sent: {}", contact);
            
            // Use the correct Google People API endpoint format
            return googlePeopleClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/people/{resourceName}:updateContact")
                            .queryParam("updatePersonFields", "names,emailAddresses,phoneNumbers")
                            .build(finalResourceName))
                    .bodyValue(contact)
                    .retrieve()
                    .bodyToMono(Contact.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error updating contact: {}, Status: {}, Response: {}", 
                    resourceName, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }
    /**
     * Deletes a contact.
     *
     * @param resourceName the resource name of the contact to delete
     */
    public void deleteContact(String resourceName) {
        try {
            log.debug("Original resourceName received: {}", resourceName);
            
            // Simple resourceName normalization
            // Remove 'people/' prefix if it exists
            final String finalResourceName;
            if (resourceName.startsWith("people/")) {
                finalResourceName = resourceName.substring("people/".length());
            } else {
                finalResourceName = resourceName;
            }
            
            log.debug("Deleting contact with normalized resourceName: {}", finalResourceName);
            
            // Use the correct Google People API endpoint format
            googlePeopleClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/people/{resourceName}:deleteContact")
                            .build(finalResourceName))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Error deleting contact: {}", resourceName, e);
            throw new RuntimeException("Failed to delete contact: " + resourceName, e);
        }
    }
    
    /**
     * Maps a Google Person object to our Contact model.
     *
     * @param personMap the Google Person object as a Map
     * @return the Contact model
     */
    private Contact mapToContact(Map<String, Object> personMap) {
        Contact contact = new Contact();
        contact.setResourceName((String) personMap.get("resourceName"));
        contact.setEtag((String) personMap.get("etag"));
        
        if (personMap.containsKey("names")) {
            List<Map<String, Object>> namesMap = (List<Map<String, Object>>) personMap.get("names");
            List<Contact.Name> names = namesMap.stream()
                    .map(this::mapToName)
                    .toList();
            contact.setNames(names);
        }
        
        if (personMap.containsKey("emailAddresses")) {
            List<Map<String, Object>> emailsMap = (List<Map<String, Object>>) personMap.get("emailAddresses");
            List<Contact.EmailAddress> emails = emailsMap.stream()
                    .map(this::mapToEmail)
                    .toList();
            contact.setEmailAddresses(emails);
        }
        
        if (personMap.containsKey("phoneNumbers")) {
            List<Map<String, Object>> phonesMap = (List<Map<String, Object>>) personMap.get("phoneNumbers");
            List<Contact.PhoneNumber> phones = phonesMap.stream()
                    .map(this::mapToPhone)
                    .toList();
            contact.setPhoneNumbers(phones);
        }
        
        return contact;
    }
    
    private Contact.Name mapToName(Map<String, Object> nameMap) {
        return Contact.Name.builder()
                .givenName((String) nameMap.get("givenName"))
                .familyName((String) nameMap.get("familyName"))
                .displayName((String) nameMap.get("displayName"))
                .build();
    }
    
    private Contact.EmailAddress mapToEmail(Map<String, Object> emailMap) {
        return Contact.EmailAddress.builder()
                .value((String) emailMap.get("value"))
                .type((String) emailMap.get("type"))
                .build();
    }
    
    private Contact.PhoneNumber mapToPhone(Map<String, Object> phoneMap) {
        return Contact.PhoneNumber.builder()
                .value((String) phoneMap.get("value"))
                .type((String) phoneMap.get("type"))
                .build();
    }
} 