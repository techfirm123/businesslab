package com.businesslab.business.domain;

import com.businesslab.workspace.domain.WorkspaceId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BusinessTest {

    private static final Instant CREATED_AT =
        Instant.parse("2026-11-11T10:00:00Z");

    private static final Instant UPDATED_AT =
        Instant.parse("2026-11-11T11:00:00Z");

    @Test
    void shouldCreateBusinessInDraftState() {
        Business business = createBusiness();

        assertThat(business.id()).isNotNull();

        assertThat(business.workspaceId()).isNotNull();

        assertThat(business.name())
            .isEqualTo("My Virtual Business");

        assertThat(business.description())
            .isEqualTo("Initial business");

        assertThat(business.templateReference().templateId())
            .isEqualTo("coffee-shop");

        assertThat(business.templateReference().version())
            .isEqualTo("1.0.0");

        assertThat(business.status())
            .isEqualTo(BusinessStatus.DRAFT);

        assertThat(business.createdAt())
            .isEqualTo(CREATED_AT);

        assertThat(business.updatedAt())
            .isEqualTo(CREATED_AT);
    }

    @Test
    void shouldActivateDraftBusiness() {
        Business business = createBusiness();

        Business activated = business.activate(UPDATED_AT);

        assertThat(activated.status())
            .isEqualTo(BusinessStatus.ACTIVE);

        assertThat(activated.updatedAt())
            .isEqualTo(UPDATED_AT);

        // Original business remains unchanged.
        assertThat(business.status())
            .isEqualTo(BusinessStatus.DRAFT);
    }

    @Test
    void shouldPauseActiveBusiness() {
        Business business = createBusiness()
            .activate(UPDATED_AT);

        Business paused = business.pause(UPDATED_AT.plusSeconds(60));

        assertThat(paused.status())
            .isEqualTo(BusinessStatus.PAUSED);
    }

    @Test
    void shouldResumePausedBusiness() {
        Business business = createBusiness()
            .activate(UPDATED_AT)
            .pause(UPDATED_AT.plusSeconds(60));

        Business resumed = business.resume(
            UPDATED_AT.plusSeconds(120)
        );

        assertThat(resumed.status())
            .isEqualTo(BusinessStatus.ACTIVE);
    }

    @Test
    void shouldCloseActiveBusiness() {
        Business business = createBusiness()
            .activate(UPDATED_AT);

        Business closed = business.close(
            UPDATED_AT.plusSeconds(60)
        );

        assertThat(closed.status())
            .isEqualTo(BusinessStatus.CLOSED);
    }

    @Test
    void shouldClosePausedBusiness() {
        Business business = createBusiness()
            .activate(UPDATED_AT)
            .pause(UPDATED_AT.plusSeconds(60));

        Business closed = business.close(
            UPDATED_AT.plusSeconds(120)
        );

        assertThat(closed.status())
            .isEqualTo(BusinessStatus.CLOSED);
    }

    @Test
    void shouldArchiveDraftBusiness() {
        Business business = createBusiness();

        Business archived = business.archive(UPDATED_AT);

        assertThat(archived.status())
            .isEqualTo(BusinessStatus.ARCHIVED);
    }

    @Test
    void shouldArchiveClosedBusiness() {
        Business business = createBusiness()
            .activate(UPDATED_AT)
            .close(UPDATED_AT.plusSeconds(60));

        Business archived = business.archive(
            UPDATED_AT.plusSeconds(120)
        );

        assertThat(archived.status())
            .isEqualTo(BusinessStatus.ARCHIVED);
    }

    @Test
    void shouldRejectActivationOfAlreadyActiveBusiness() {
        Business business = createBusiness()
            .activate(UPDATED_AT);

        assertThatThrownBy(
            () -> business.activate(UPDATED_AT.plusSeconds(60))
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only draft businesses can be activated");
    }

    @Test
    void shouldRejectPausingDraftBusiness() {
        Business business = createBusiness();

        assertThatThrownBy(
            () -> business.pause(UPDATED_AT)
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only active businesses can be paused");
    }

    @Test
    void shouldRejectArchivingActiveBusiness() {
        Business business = createBusiness()
            .activate(UPDATED_AT);

        assertThatThrownBy(
            () -> business.archive(UPDATED_AT.plusSeconds(60))
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(
                "Active businesses must be paused or closed before archiving"
            );
    }

    @Test
    void shouldNotAllowModificationsAfterClosingBusiness() {
        Business business = createBusiness()
            .activate(UPDATED_AT)
            .close(UPDATED_AT.plusSeconds(60));

        assertThatThrownBy(
            () -> business.rename(
                "Renamed Business",
                UPDATED_AT.plusSeconds(120)
            )
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(
                "Closed or archived businesses cannot be modified"
            );
    }

    @Test
    void shouldNotAllowModificationsAfterArchivingBusiness() {
        Business business = createBusiness()
            .archive(UPDATED_AT);

        assertThatThrownBy(
            () -> business.updateConfiguration(
                BusinessConfiguration.empty(),
                UPDATED_AT.plusSeconds(60)
            )
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(
                "Closed or archived businesses cannot be modified"
            );
    }

    @Test
    void shouldAllowRenamingActiveBusiness() {
        Business business = createBusiness()
            .activate(UPDATED_AT);

        Business renamed = business.rename(
            "New Business Name",
            UPDATED_AT.plusSeconds(60)
        );

        assertThat(renamed.name())
            .isEqualTo("New Business Name");

        assertThat(renamed.status())
            .isEqualTo(BusinessStatus.ACTIVE);
    }

    @Test
    void shouldNormalizeBusinessNameAndDescription() {
        Business business = Business.create(
            BusinessId.generate(),
            WorkspaceId.generate(),
            new BusinessTemplateReference(
                "coffee-shop",
                "1.0.0"
            ),
            "  My Business  ",
            "  My Description  ",
            BusinessConfiguration.empty(),
            CREATED_AT
        );

        assertThat(business.name())
            .isEqualTo("My Business");

        assertThat(business.description())
            .isEqualTo("My Description");
    }

    @Test
    void shouldDefaultNullDescriptionToEmptyString() {
        Business business = Business.create(
            BusinessId.generate(),
            WorkspaceId.generate(),
            new BusinessTemplateReference(
                "coffee-shop",
                "1.0.0"
            ),
            "My Business",
            null,
            BusinessConfiguration.empty(),
            CREATED_AT
        );

        assertThat(business.description()).isEmpty();
    }

    @Test
    void shouldRejectBlankBusinessName() {
        assertThatThrownBy(
            () -> Business.create(
                BusinessId.generate(),
                WorkspaceId.generate(),
                new BusinessTemplateReference(
                    "coffee-shop",
                    "1.0.0"
                ),
                " ",
                null,
                BusinessConfiguration.empty(),
                CREATED_AT
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Business name cannot be blank");
    }

    @Test
    void shouldRejectUpdateTimestampBeforePreviousUpdate() {
        Business business = createBusiness();

        assertThatThrownBy(
            () -> business.activate(CREATED_AT.minusSeconds(1))
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "Update timestamp cannot be before the previous update"
            );
    }

    private Business createBusiness() {
        return Business.create(
            BusinessId.generate(),
            WorkspaceId.generate(),
            new BusinessTemplateReference(
                "coffee-shop",
                "1.0.0"
            ),
            "My Virtual Business",
            "Initial business",
            BusinessConfiguration.of(
                Map.of(
                    "initialCapital", 100000,
                    "currency", "INR"
                )
            ),
            CREATED_AT
        );
    }
}
