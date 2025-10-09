import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * MatchMaker.java - versión simplificada sin listas
 */
public class MatchMaker {

    private static final int PREMIUM_MATCHES_PER_CYCLE = 2;

    private final PriorityQueue<RequestV2> premiumLong;
    private final PriorityQueue<RequestV2> premiumShort;
    private final Queue<RequestV2> nonPremiumLong;
    private final Queue<RequestV2> nonPremiumShort;

    public MatchMaker() {
        Comparator<RequestV2> bySkillDesc = Comparator.comparingInt(RequestV2::getSkillLevel).reversed();
        premiumLong = new PriorityQueue<>(bySkillDesc);
        premiumShort = new PriorityQueue<>(bySkillDesc);
        nonPremiumLong = new LinkedList<>();
        nonPremiumShort = new LinkedList<>();
    }

    public void enqueue(RequestV2 r) {
        if (r.isPremiumSubscription()) {
            if (r.getMatchType() == MatchType.LONG) premiumLong.offer(r);
            else premiumShort.offer(r);
        } else {
            if (r.getMatchType() == MatchType.LONG) nonPremiumLong.offer(r);
            else nonPremiumShort.offer(r);
        }
    }

    public void processAndPrintMatches() {
        System.out.println("\n--- Creación de matches según colas y prioridades ---\n");
        int cycle = 1;
        boolean anyCreated;

        do {
            System.out.printf("Ciclo %d:\n", cycle);
            anyCreated = false;

            // Premium long
            for (int i = 0; i < PREMIUM_MATCHES_PER_CYCLE; i++) {
                if (premiumLong.size() >= 2) {
                    printMatch(new Match(true, MatchType.LONG, premiumLong.poll(), premiumLong.poll()));
                    anyCreated = true;
                } else break;
            }

            // Premium short
            for (int i = 0; i < PREMIUM_MATCHES_PER_CYCLE; i++) {
                if (premiumShort.size() >= 2) {
                    printMatch(new Match(true, MatchType.SHORT, premiumShort.poll(), premiumShort.poll()));
                    anyCreated = true;
                } else break;
            }

            // Non-premium long
            if (nonPremiumLong.size() >= 2) {
                printMatch(new Match(false, MatchType.LONG, nonPremiumLong.poll(), nonPremiumLong.poll()));
                anyCreated = true;
            }

            // Non-premium short
            if (nonPremiumShort.size() >= 2) {
                printMatch(new Match(false, MatchType.SHORT, nonPremiumShort.poll(), nonPremiumShort.poll()));
                anyCreated = true;
            }

            if (!anyCreated) {
                System.out.println("  (No se pudieron crear más matches en este ciclo.)");
            }
            System.out.println();
            cycle++;
        } while (canCreateAnyMatch());
        System.out.println("--- Procesamiento finalizado ---");
    }

    private boolean canCreateAnyMatch() {
        return premiumLong.size() >= 2 ||
               premiumShort.size() >= 2 ||
               nonPremiumLong.size() >= 2 ||
               nonPremiumShort.size() >= 2;
    }

    private void printMatch(Match m) {
        String premiumStr = m.isPremium() ? "PREMIUM" : "No premium";
        String typeStr = (m.getType() == MatchType.LONG) ? "Largo (30')" : "Corto (10')";
        RequestV2 a = (RequestV2) m.getPlayerA();
        RequestV2 b = (RequestV2) m.getPlayerB();
        System.out.printf("  MATCH - %s - %s: %s(%d) vs %s(%d)%n",
                premiumStr, typeStr, a.getPlayerID(), a.getSkillLevel(), b.getPlayerID(), b.getSkillLevel());
    }

    public static void main(String[] args) {
        String filename = (args.length > 0) ? args[0] : "player_requests.csv";
        MatchMaker mm = new MatchMaker();

        System.out.println("--- Lectura de peticiones desde " + filename + " ---\n");
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;
                if (lineNumber == 1 && line.toLowerCase().contains("requestid")) continue;
                try {
                    RequestV2 req = RequestV2.parseFromCsv(line); // Parse once
                    mm.enqueue(req);
                    System.out.println("Request leída: " + req);
                } catch (IllegalArgumentException ex) {
                    System.err.printf("Línea %d inválida -> %s : %s%n", lineNumber, line, ex.getMessage());
                }
            }
        } catch (IOException ioe) {
            System.err.println("Error leyendo fichero: " + ioe.getMessage());
            return;
        }

        mm.processAndPrintMatches();
    }
}

enum MatchType {
    LONG, SHORT
}

class RequestV2 {
    private final String requestID;
    private final String playerID;
    private final boolean premiumSubscription;
    private final int skillLevel;
    private final MatchType matchType;

    public RequestV2(String requestID, String playerID, boolean premiumSubscription, int skillLevel, MatchType matchType) {
        this.requestID = requestID;
        this.playerID = playerID;
        this.premiumSubscription = premiumSubscription;
        this.skillLevel = skillLevel;
        this.matchType = matchType;
    }

    public static RequestV2 parseFromCsv(String line) {
        String[] parts = line.split(";");
        if (parts.length != 5) throw new IllegalArgumentException("Formato CSV incorrecto");
        String reqId = parts[0].trim();
        String playerId = parts[1].trim();
        boolean premium = Boolean.parseBoolean(parts[2].trim());
        int skill = Integer.parseInt(parts[3].trim());
        MatchType type = parts[4].trim().equalsIgnoreCase("L") ? MatchType.LONG : MatchType.SHORT;
        return new RequestV2(reqId, playerId, premium, skill, type);
    }

    public String getRequestID() { return requestID; }
    public String getPlayerID() { return playerID; }
    public boolean isPremiumSubscription() { return premiumSubscription; }
    public int getSkillLevel() { return skillLevel; }
    public MatchType getMatchType() { return matchType; }

    @Override
    public String toString() {
        return String.format("%s; %s; Premium: %b; Skill: %d; Type: %s",
                requestID, playerID, premiumSubscription, skillLevel, matchType);
    }
}

class Match {
    private final boolean premium;
    private final MatchType type;
    private final Object playerA;
    private final Object playerB;

    public Match(boolean premium, MatchType type, Object playerA, Object playerB) {
        this.premium = premium;
        this.type = type;
        this.playerA = playerA;
        this.playerB = playerB;
    }

    public boolean isPremium() { return premium; }
    public MatchType getType() { return type; }
    public Object getPlayerA() { return playerA; }
    public Object getPlayerB() { return playerB; }
}