package com.example.AfriMarket_backend.config;

import com.example.AfriMarket_backend.model.*;
import com.example.AfriMarket_backend.model.enums.*;
import com.example.AfriMarket_backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final ZoneRepository zoneRepo;
    private final RelayPointRepository relayRepo;
    private final OfferRepository offerRepo;
    private final OrderRepository orderRepo;
    private final TransactionRepository txRepo;
    private final DisputeRepository disputeRepo;
    private final SystemSettingRepository settingRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(UserRepository userRepo, ZoneRepository zoneRepo,
                           RelayPointRepository relayRepo, OfferRepository offerRepo,
                           OrderRepository orderRepo, TransactionRepository txRepo,
                           DisputeRepository disputeRepo, SystemSettingRepository settingRepo,
                           PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.zoneRepo = zoneRepo;
        this.relayRepo = relayRepo;
        this.offerRepo = offerRepo;
        this.orderRepo = orderRepo;
        this.txRepo = txRepo;
        this.disputeRepo = disputeRepo;
        this.settingRepo = settingRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) return; // already seeded

        // --- Zones ---
        Zone yde1 = zone("Yaoundé 1", "Yaoundé", "Centre");
        Zone yde3 = zone("Yaoundé 3 - Efoulan", "Yaoundé", "Centre");
        Zone yde4 = zone("Yaoundé 4 - Mvog-Ada", "Yaoundé", "Centre");
        Zone yde6 = zone("Yaoundé 6 - Biyem-Assi", "Yaoundé", "Centre");
        Zone mbalmayo = zone("Mbalmayo", "Mbalmayo", "Centre");
        Zone obala = zone("Obala", "Obala", "Centre");
        zoneRepo.saveAll(List.of(yde1, yde3, yde4, yde6, mbalmayo, obala));

        // --- Admin user ---
        User admin = new User();
        admin.setUsername("admin");
        admin.setFullName("Administrateur AfriMarket");
        admin.setPhone("+237699000001");
        admin.setPasswordHash(encoder.encode("Admin@2026"));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setZone(yde1);
        userRepo.save(admin);

        // --- Finance user ---
        User finance = new User();
        finance.setUsername("finance");
        finance.setFullName("Gestionnaire Financier");
        finance.setPhone("+237699000002");
        finance.setPasswordHash(encoder.encode("Finance@2026"));
        finance.setRole(UserRole.FINANCE);
        finance.setStatus(UserStatus.ACTIVE);
        userRepo.save(finance);

        // --- Producers ---
        User prod1 = producer("Jean-Pierre Mbarga", "+237677123401", mbalmayo, "Jean-Pierre_Mbarga");
        User prod2 = producer("Marie Atangana", "+237677123402", obala, "Marie_Atangana");
        User prod3 = producer("Emmanuel Fouda", "+237677123403", mbalmayo, "Emmanuel_Fouda");
        userRepo.saveAll(List.of(prod1, prod2, prod3));

        // --- Consumers ---
        User cons1 = consumer("Alice Ngo", "+237655200101", yde6);
        User cons2 = consumer("Robert Essono", "+237655200102", yde4);
        User cons3 = consumer("Fatima Diallo", "+237655200103", yde3);
        User cons4 = consumer("Paul Nkemdirim", "+237655200104", yde6);
        userRepo.saveAll(List.of(cons1, cons2, cons3, cons4));

        // Pending producer (awaiting validation)
        User pendingProd = producer("Sylvie Ondoa", "+237677123410", obala, null);
        pendingProd.setStatus(UserStatus.PENDING);
        userRepo.save(pendingProd);

        // --- Relay points ---
        RelayPoint r1 = relay("Boutique Mfoundi Centre", "Alain Ekwalla", "+237655001001", "Rue de Mfoundi, Yaoundé 3", 3.8593, 11.5218, yde3, 200.0);
        RelayPoint r2 = relay("Pharmacie Biyem-Assi", "Dr. Nkono", "+237655001002", "Av. Kennedy, Biyem-Assi", 3.8234, 11.5012, yde6, 150.0);
        RelayPoint r3 = relay("Epicerie Mvog-Ada", "Mama Tchoumi", "+237655001003", "Carrefour Mvog-Ada", 3.8412, 11.5321, yde4, 100.0);
        relayRepo.saveAll(List.of(r1, r2, r3));

        // --- Offers ---
        Offer o1 = offer(prod1, "Tomates cerises Mbalmayo - Récolte fraîche", OfferCategory.VEGETABLES,
                OfferUnit.KG, 850, 200, 80, 2, 60, OfferStatus.ACTIVE, 7);
        o1.setCurrentQtyOrdered(BigDecimal.valueOf(42));

        Offer o2 = offer(prod2, "Plantains verts Obala - Lot familial", OfferCategory.FRUITS,
                OfferUnit.KG, 400, 300, 100, 5, 75, OfferStatus.THRESHOLD_REACHED, 5);
        o2.setCurrentQtyOrdered(BigDecimal.valueOf(100));

        Offer o3 = offer(prod3, "Manioc blanc séché - qualité supérieure", OfferCategory.TUBERS,
                OfferUnit.KG, 600, 150, 60, 3, 15, OfferStatus.PENDING_REVIEW, 14);

        Offer o4 = offer(prod1, "Maïs grain local - sac 50kg", OfferCategory.CEREALS,
                OfferUnit.KG, 320, 500, 150, 10, 80, OfferStatus.DELIVERING, 3);
        o4.setCurrentQtyOrdered(BigDecimal.valueOf(150));

        Offer o5 = offer(prod2, "Gombo frais - botte de saison", OfferCategory.VEGETABLES,
                OfferUnit.BUNCH, 250, 100, 30, 2, 5, OfferStatus.COMPLETED, 7);
        o5.setCurrentQtyOrdered(BigDecimal.valueOf(30));

        Offer o6 = offer(prod3, "Ignames blanches - Récolte octobre", OfferCategory.TUBERS,
                OfferUnit.KG, 500, 200, 70, 5, 20, OfferStatus.PENDING_REVIEW, 14);

        offerRepo.saveAll(List.of(o1, o2, o3, o4, o5, o6));

        // --- Orders ---
        Order ord1 = order(o2, cons1, r2, 20, 400, OrderStatus.AT_RELAY, MomoProvider.MTN);
        Order ord2 = order(o2, cons2, r1, 15, 400, OrderStatus.AT_RELAY, MomoProvider.ORANGE);
        Order ord3 = order(o1, cons3, r1, 5, 850, OrderStatus.PAID, MomoProvider.MTN);
        Order ord4 = order(o5, cons4, r3, 4, 250, OrderStatus.COMPLETED, MomoProvider.MTN);
        ord4.setCompletedAt(LocalDateTime.now().minusDays(1));
        Order ord5 = order(o4, cons1, r2, 20, 320, OrderStatus.DELIVERING, MomoProvider.ORANGE);
        orderRepo.saveAll(List.of(ord1, ord2, ord3, ord4, ord5));

        // --- Transactions ---
        tx(ord1, TransactionType.CAPTURE, 8000, 320, 7680);
        tx(ord2, TransactionType.CAPTURE, 6000, 240, 5760);
        tx(ord4, TransactionType.RELEASE, 1000, 40, 960);
        txRepo.saveAll(List.of());

        // --- Disputes ---
        Dispute d1 = new Dispute();
        d1.setOrder(ord3);
        d1.setOpenedBy(cons3);
        d1.setReason("Produit non conforme");
        d1.setDescription("Les tomates reçues étaient trop mûres et abîmées.");
        d1.setStatus(DisputeStatus.OPEN);
        disputeRepo.save(d1);

        // --- System settings ---
        settingRepo.saveAll(List.of(
            new SystemSetting("commission_rate", "4.0", "Taux de commission AfriMarket (%)"),
            new SystemSetting("min_withdrawal", "500", "Seuil minimum de reversement auto (FCFA)"),
            new SystemSetting("auto_confirm_days", "1", "Jours avant auto-confirmation réception"),
            new SystemSetting("max_offer_duration", "14", "Durée maximale d'une offre (jours)"),
            new SystemSetting("admin_validation_hours", "24", "Heures avant auto-approbation offre"),
            new SystemSetting("max_active_offers", "5", "Nombre max d'offres ACTIVE simultanées par producteur"),
            new SystemSetting("sms_provider", "africas_talking", "Fournisseur SMS (africas_talking / twilio / orange)"),
            new SystemSetting("escrow_max_days", "7", "Durée maximale de conservation en escrow (jours)")
        ));
    }

    private Zone zone(String name, String city, String region) {
        return new Zone(name, city, region);
    }

    private User producer(String name, String phone, Zone zone, String username) {
        User u = new User();
        u.setFullName(name);
        u.setPhone(phone);
        u.setUsername(username);
        u.setPasswordHash(encoder.encode("Prod@2026"));
        u.setRole(UserRole.PRODUCER);
        u.setStatus(UserStatus.ACTIVE);
        u.setZone(zone);
        u.setMomoProvider(MomoProvider.MTN);
        u.setMomoNumber(phone);
        u.setRatingAvg(4.2);
        u.setRatingCount(12);
        return u;
    }

    private User consumer(String name, String phone, Zone zone) {
        User u = new User();
        u.setFullName(name);
        u.setPhone(phone);
        u.setRole(UserRole.CONSUMER);
        u.setStatus(UserStatus.ACTIVE);
        u.setZone(zone);
        u.setMomoProvider(MomoProvider.MTN);
        u.setMomoNumber(phone);
        u.setRatingAvg(4.5);
        u.setRatingCount(5);
        return u;
    }

    private RelayPoint relay(String name, String manager, String phone, String address,
                              double lat, double lng, Zone zone, double cap) {
        RelayPoint r = new RelayPoint();
        r.setName(name);
        r.setManagerName(manager);
        r.setPhone(phone);
        r.setAddress(address);
        r.setGpsLat(lat);
        r.setGpsLng(lng);
        r.setZone(zone);
        r.setCapacityKg(cap);
        r.setStatus(RelayStatus.ACTIVE);
        r.setScheduleJson("{\"mon\":\"08:00-18:00\",\"tue\":\"08:00-18:00\",\"wed\":\"08:00-18:00\",\"thu\":\"08:00-18:00\",\"fri\":\"08:00-18:00\",\"sat\":\"09:00-14:00\"}");
        return r;
    }

    private Offer offer(User producer, String title, OfferCategory cat, OfferUnit unit,
                         int price, int qty, int threshold, int minPerBuyer, int daysUntilAvail,
                         OfferStatus status, int validDays) {
        Offer o = new Offer();
        o.setProducer(producer);
        o.setTitle(title);
        o.setCategory(cat);
        o.setUnit(unit);
        o.setPricePerUnit(BigDecimal.valueOf(price));
        o.setAvailableQty(BigDecimal.valueOf(qty));
        o.setMinThreshold(BigDecimal.valueOf(threshold));
        o.setMinQtyPerBuyer(BigDecimal.valueOf(minPerBuyer));
        o.setCurrentQtyOrdered(BigDecimal.ZERO);
        o.setStatus(status);
        o.setAvailableFrom(LocalDate.now().plusDays(daysUntilAvail));
        o.setExpiresAt(LocalDateTime.now().plusDays(validDays));
        return o;
    }

    private Order order(Offer offer, User buyer, RelayPoint relay, int qty, int price,
                         OrderStatus status, MomoProvider provider) {
        Order o = new Order();
        o.setOffer(offer);
        o.setBuyer(buyer);
        o.setRelay(relay);
        o.setQtyOrdered(BigDecimal.valueOf(qty));
        o.setUnitPriceSnapshot(BigDecimal.valueOf(price));
        o.setTotalAmount(BigDecimal.valueOf((long) qty * price));
        o.setStatus(status);
        o.setPaymentProvider(provider);
        o.setPaymentRef("REF" + System.nanoTime());
        o.setPaidAt(LocalDateTime.now().minusDays(2));
        return o;
    }

    private void tx(Order order, TransactionType type, long amount, long commission, long net) {
        Transaction t = new Transaction();
        t.setOrder(order);
        t.setType(type);
        t.setAmount(BigDecimal.valueOf(amount));
        t.setCommission(BigDecimal.valueOf(commission));
        t.setNetAmount(BigDecimal.valueOf(net));
        t.setMomoRef("MOMO" + System.nanoTime());
        t.setStatus("COMPLETED");
        txRepo.save(t);
    }
}
