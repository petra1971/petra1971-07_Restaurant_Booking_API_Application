package se.edugrade.java25.restaurant.assembler;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import se.edugrade.java25.restaurant.controller.BookingController;
import se.edugrade.java25.restaurant.dto.BookingResource;
import se.edugrade.java25.restaurant.model.Booking;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * HATEOAS – servern skickar möjliga nästa steg som länker.
 * Syfte med klassen - Att omvandla en Booking till en EntityModel<BookingResource> och
 * lägga till de HATEOAS-länkar som är tillåtna utifrån bokningens status.
 *
 * <p>{@code toCollectionModel} comes free as a default method that calls
 * {@code toModel} per element, so the list endpoint carries no link code at
 * all.</p>
 *
 * <p>Every link here is built with {@code methodOn}, never with string
 * concatenation. {@code Link.of("/tasks/" + id, "self")} compiles happily
 * forever, including after someone moves the endpoint. The proxy version stops
 * compiling instead, which is the entire point.</p>
 */
@Component
public class BookingModelAssembler implements RepresentationModelAssembler<Booking, EntityModel<BookingResource>> {

    @Override
    public EntityModel<BookingResource> toModel(Booking booking) {
        return EntityModel.of(BookingResource.from(booking), linksFor(booking));
    }

    /** The single source of truth for a task's links, whatever contract wraps it. */
    private List<Link> linksFor(Booking booking) {
        List<Link> links = new ArrayList<>();

        links.add(linkTo(methodOn(BookingController.class).oneById(booking.getId(), null, null))
                .withSelfRel());

        links.add(linkTo(methodOn(BookingController.class).allBookings(0, 5))
                .withRel(IanaLinkRelations.COLLECTION)); // .withRel - "vad respresenterar den här länken". IanaLinkRelations.COLLECTION - "länken leder till samlingen/collectionen som den här resursen tillhör."

        if (booking.canConfirm()) {
            links.add(linkTo(methodOn(BookingController.class).confirm(booking.getId()))
                    .withRel("confirm").withTitle("Confirm this booking"));
        }
        if (booking.canSeat()) {
            links.add(linkTo(methodOn(BookingController.class).seat(booking.getId()))
                    .withRel("seat").withTitle("Seat the guests in this booking"));
        }
        if (booking.canCancel()) {
            links.add(linkTo(methodOn(BookingController.class).cancel(booking.getId()))
                    .withRel("cancel").withTitle("Cancel this booking"));
        }

        // Absence is the message. A CANCELLED task reaches this line with only
        // self and collection, and no "isFinished": true flag is needed to say
        // so.
        return links;
    }

    public PagedModel<EntityModel<BookingResource>> toPagedModel(Page<Booking> result) {

        int page = result.getNumber();
        int size = result.getSize();

        // Återanvänd befintlig toModel() för varje bokning
        List<EntityModel<BookingResource>> items =
                result.getContent().stream()
                        .map(booking -> this.toModel(booking))
                        .toList();

        // Skapa information om pagineringen
        var metadata = new PagedModel.PageMetadata(
                size,
                page,
                result.getTotalElements(),
                result.getTotalPages()
        );

        PagedModel<EntityModel<BookingResource>> model =
                PagedModel.of(items, metadata);

        // Skapa länkar till samlingens sidor
        model.add(pageLink(page, size, IanaLinkRelations.SELF));

        model.add(pageLink(0, size, IanaLinkRelations.FIRST));

        if (result.getTotalPages() > 0) {
            model.add(pageLink(
                    result.getTotalPages() - 1,
                    size,
                    IanaLinkRelations.LAST));
        }

        if (result.hasPrevious()) {
            model.add(pageLink(
                    page - 1,
                    size,
                    IanaLinkRelations.PREV));
        }

        if (result.hasNext()) {
            model.add(pageLink(
                    page + 1,
                    size,
                    IanaLinkRelations.NEXT));
        }

        return model;
    }

    private Link pageLink(int page, int size, LinkRelation relation) {
        return linkTo(methodOn(BookingController.class)
                .allBookings(page, size))
                .withRel(relation);
    }
}
