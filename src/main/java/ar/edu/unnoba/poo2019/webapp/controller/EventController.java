/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ar.edu.unnoba.poo2019.webapp.controller;

import ar.edu.unnoba.poo2019.webapp.model.Event;
import ar.edu.unnoba.poo2019.webapp.model.Invite;
import ar.edu.unnoba.poo2019.webapp.model.Payment;
import ar.edu.unnoba.poo2019.webapp.model.Registration;
import ar.edu.unnoba.poo2019.webapp.service.EventService;
import ar.edu.unnoba.poo2019.webapp.service.InviteService;
import ar.edu.unnoba.poo2019.webapp.service.PaymentService;
import ar.edu.unnoba.poo2019.webapp.service.RegistrationService;
import ar.edu.unnoba.poo2019.webapp.service.SessionService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author root
 */
@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private InviteService inviteService;

    @GetMapping
    public String index(Model model) {
        List<Event> events = eventService.events();
        model.addAttribute("events", events);
        model.addAttribute("currentUser", sessionService.getCurrentUser());
        return "events/index";
    }

    @GetMapping("/myEvents")
    public String myEvents(Model model) {
        List<Event> events = eventService.findEventsByOwnerId(sessionService.getCurrentUser().getId());
        model.addAttribute("events", events);
        model.addAttribute("currentUser", sessionService.getCurrentUser());
        return "events/myEvents";
    }

    @GetMapping("/new")
    public String eventNew(Model model) {
        model.addAttribute("event", new Event());
        
        return "events/new";
    }

    @PostMapping
    public String create(@ModelAttribute Event event, Model model) throws Exception {
        try {
            eventService.create(event);
            return "redirect:/events/myEvents";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "/error/error";
        }

    }

    @GetMapping("/{id}/delete")
    public String delete(Model model, @PathVariable("id") Long id) throws Exception {
        try {
            Event event = eventService.find(id);
            if (Objects.equals(sessionService.getCurrentUser().getId(), event.getOwner().getId())) {    // Controlo que sea el propio usuario 
                if (inviteService.findByEvent(event).isEmpty()) {
                    eventService.delete(id);
                    return "redirect:/events/myEvents";
                }
                throw new Exception("No se puede Borrar por que el evento posee invitaciones");
            }

            throw new Exception("Permiso denegado usuario invalido");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "/error/error";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) throws Exception {
        try {
            Event event = eventService.find(id);
            if (Objects.equals(sessionService.getCurrentUser().getId(), event.getOwner().getId())) {  // Controlo que sea el propio usuario 
                model.addAttribute("event", event);
                return "events/edit";
            }
            throw new Exception("Permiso denegado usuario invalido");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "/error/error";
        }
    }

    @PostMapping("/{id}/update")
    public String update(Model model, @PathVariable Long id, @ModelAttribute Event event) throws Exception {
        try {
            Event oldEvent = eventService.find(id);
            if (Objects.equals(sessionService.getCurrentUser().getId(), oldEvent.getOwner().getId())) {    // Controlo que sea el propio usuario 
                eventService.update(id, event);
                return "redirect:/events/myEvents";
            }

            throw new Exception("Permiso denegado usuario invalido");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "/error/error";
        }
    }

    @GetMapping("/{id}/eventDetails")
    public String detail(@PathVariable Long id, Model model) throws Exception {
        try {
            Event event = eventService.find(id);
            if (Objects.equals(sessionService.getCurrentUser().getId(), event.getOwner().getId())) {  // Controlo que sea el propio usuario 
                List<Invite> invites = inviteService.findByEvent(event);
                model.addAttribute("event", event);
                model.addAttribute("invites", invites);
                model.addAttribute("currentUser", sessionService.getCurrentUser());

                if (event.getCost() > 0) {
                    List<Payment> payments = paymentService.findByEvent(event);
                    model.addAttribute("payments", payments);
                    return "events/eventDetailsPago";
                }
                List<Registration> registrations = event.getRegistrations();
                model.addAttribute("registrations", registrations);
                return "events/eventDetailsGratis";
            }
            throw new Exception("Permiso denegado usuario invalido");

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "/error/error";
        }
    }
        // Ver si se puede poner en AppConfiguration o si se puede hacer otra cosa
        @InitBinder
        public void initBinder
        (final WebDataBinder binder
        
            ) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
        }

    }
