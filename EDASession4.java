import java.io.*;
import java.util.*;

/**
 * EdaSession4 - ProFootball 2025 Matchmaking Simulation
 * Authors: Silvia Mª Arroyo Zaballos, Carlos Ruíz Velasco, Sergio Trujillo López, Team: EDPA1-3-01
 */
public class EDASession4 {
    public static void main(String[] args) {
        // Different queues for premium and not premium
        SimpleQueue premiumLong = new SimpleQueue(200);
        SimpleQueue premiumShort = new SimpleQueue(200);
        SimpleQueue nonPremiumLong = new SimpleQueue(200);
        SimpleQueue nonPremiumShort = new SimpleQueue(200);
        StringBuilder printed = new StringBuilder();

        // Locate CSV automatically
        File csv = null;
        if (args != null && args.length > 0) {
            File f = new File(args[0]);
            if (f.exists() && f.isFile()) csv = f;
        }
        if (csv == null) csv = findCsv("player_requests.csv");
        if (csv == null) { System.out.println("player_requests.csv not found."); return; }

        // Reads the requests and sorts into the four possible queues
        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] p = line.split(";");
                if (p.length < 5) continue;
                boolean premium = Boolean.parseBoolean(p[2].trim());
                int skill;
                try { skill = Integer.parseInt(p[3].trim()); } catch (Exception ex) { continue; }
                Request r = new Request(p[0].trim(), p[1].trim(), premium, skill, p[4].trim().isEmpty()?'S':p[4].trim().charAt(0));
                printed.append(r).append("\n");
                if (premium) { // for premium
                    if (r.getMatchType() == 'L') premiumLong.add(r); else premiumShort.add(r);
                } else { // for not premium
                    if (r.getMatchType() == 'L') nonPremiumLong.add(r); else nonPremiumShort.add(r);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }

        // Print all requests
        System.out.println("Requests:\n" + printed);

        // Sort premium queues by skill descending
        premiumLong.sortBySkillDesc();
        premiumShort.sortBySkillDesc();

        // Data related to process matches
        processMatches(premiumLong, "Premium", "Long", 2);
        processMatches(premiumShort, "Premium", "Short", 2);
        processMatches(nonPremiumLong, "Non-premium", "Long", 1);
        processMatches(nonPremiumShort, "Non-premium", "Short", 1);
    }

    // Processes matches from the given queue
    private static void processMatches(SimpleQueue q, String type, String matchType, int matchesPerCycle) {
        while (q.size() >= 2) {
            for (int i = 0; i < matchesPerCycle && q.size() >= 2; i++) {
                Request r1 = q.poll();
                Request r2 = q.poll();
                System.out.printf("%s %s Match: %s (Skill %d) vs %s (Skill %d)%n",
                        type, matchType, r1.getPlayerId(), r1.getSkillLevel(), r2.getPlayerId(), r2.getSkillLevel());
            }
        }
    }

    // Finds CSV file automatically 
    private static File findCsv(String target) {
        File[] bases = {
            new File("."), 
            new File(System.getProperty("user.home", ".")) 
        };
        String cp = System.getProperty("java.class.path");
        if (cp != null) {
            String[] paths = cp.split(System.getProperty("path.separator"));
            File[] newBases = new File[bases.length + paths.length];
            System.arraycopy(bases, 0, newBases, 0, bases.length);
            for (int i = 0; i < paths.length; i++) {
                File f = new File(paths[i]);
                newBases[bases.length + i] = f.isDirectory() ? f : f.getParentFile();
            }
            bases = newBases;
        }
        for (File b : bases) {
            File f = bfsSearch(b, target);
            if (f != null) return f;
        }
        return null;
    }

    // Search for a file name
    private static File bfsSearch(File base, String name) {
        if (base == null || !base.exists()) return null;
        ArrayDeque<File> q = new ArrayDeque<>();
        q.add(base);
        while (!q.isEmpty()) {
            File cur = q.poll();
            File[] files = cur.listFiles();
            if (files == null) continue;
            for (File f : files) {
                if (f.isFile() && f.getName().equalsIgnoreCase(name)) return f;
                if (f.isDirectory()) q.add(f);
            }
        }
        return null;
    }
}

// Modify the queue using a size fixed with an array
class SimpleQueue {
    private final Request[] data;
    private int head = 0, tail = 0, count = 0;
    public SimpleQueue(int capacity) { data = new Request[capacity]; }

    public void add(Request r) { if (count < data.length) { data[tail] = r; tail = (tail + 1) % data.length; count++; } }
    public Request poll() {
        if (count == 0) return null;
        Request r = data[head];
        head = (head + 1) % data.length;
        count--;
        return r;
    }
    public int size() { return count; }
    public boolean isEmpty() { return count == 0; }

    // Sorts queue content by descending skill for premium queues
    public void sortBySkillDesc() {
        Request[] temp = toArray();
        Arrays.sort(temp, (a, b) -> Integer.compare(b.getSkillLevel(), a.getSkillLevel()));
        clear();
        for (Request r : temp) add(r);
    }

    private Request[] toArray() {
        Request[] arr = new Request[count];
        for (int i = 0; i < count; i++) arr[i] = data[(head + i) % data.length];
        return arr;
    }

    private void clear() { head = 0; tail = 0; count = 0; }
}

// Data related to the match requests
class Request {
    private final String requestId, playerId;
    private final boolean premium;
    private final int skillLevel;
    private final char matchType;
    public Request(String id, String pid, boolean prem, int skill, char type) {
        requestId = id; playerId = pid; premium = prem; skillLevel = skill; matchType = type;
    }
    public String getRequestId() { return requestId; }
    public String getPlayerId() { return playerId; }
    public boolean isPremium() { return premium; }
    public int getSkillLevel() { return skillLevel; }
    public char getMatchType() { return matchType; }
    public String toString() { return String.format("%s; %s; Premium: %b; Skill: %d; Type: %c",
            requestId, playerId, premium, skillLevel, matchType); }
}
