package org.midtermactivity.midtermactivity.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.midtermactivity.midtermactivity.model.Contact;
import org.midtermactivity.midtermactivity.service.ContactService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@Slf4j
public class ContactController {

    private final ContactService contactService;

    /**
     * Retrieves all contacts from the authenticated user's Google Contacts.
     *
     * @return List of contacts
     */
    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        List<Contact> contacts = contactService.getAllContacts();
        if (contacts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(contacts);
    }

    /**
     * Retrieves a single contact by resource name.
     *
     * @param resourceName the resource name of the contact
     * @return the contact
     */
    @GetMapping("/{resourceName}")
    public ResponseEntity<Contact> getContact(@PathVariable String resourceName) {
        Contact contact = contactService.getContact(resourceName);
        if (contact == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contact);
    }

    /**
     * Creates a new contact.
     *
     * @param contact the contact to create
     * @return the created contact
     */
    @PostMapping
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        try {
            Contact createdContact = contactService.createContact(contact);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdContact);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates an existing contact.
     *
     * @param resourceName the resource name of the contact to update
     * @param contact the updated contact information
     * @return the updated contact
     */
    @PutMapping("/{resourceName}")
    public ResponseEntity<Contact> updateContact(@PathVariable String resourceName, 
                                                @RequestBody Contact contact) {
        Contact updatedContact = contactService.updateContact(resourceName, contact);
        if (updatedContact == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(updatedContact);
    }

    /**
     * Deletes a contact.
     *
     * @param resourceName the resource name of the contact to delete
     * @return no content
     */
    @DeleteMapping("/{resourceName}")
    public ResponseEntity<Void> deleteContact(@PathVariable String resourceName) {
        contactService.deleteContact(resourceName);
        return ResponseEntity.noContent().build();
    }
} 