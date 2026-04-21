package tests;

import model.Combat.CombatUnit;

/**
 * Classe de test pour valider les fonctionnalités de CombatUnit
 * Version standalone sans dépendance JUnit - Lance les tests directement
 */
public class TestCombatUnit {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private CombatUnit combatUnit;

    public static void main(String[] args) {
        TestCombatUnit testSuite = new TestCombatUnit();
        
        System.out.println("========================================");
        System.out.println("  Tests CombatUnit - Début de l'exécution");
        System.out.println("========================================\n");

        testSuite.testInitialHealth();
        testSuite.testInitialAttackPower();
        testSuite.testIsAliveAtStart();
        testSuite.testReceiveDamage();
        testSuite.testMultipleDamage();
        testSuite.testUnitDeath();
        testSuite.testHealthNotNegative();
        testSuite.testUnitStaysAliveWithHealth();
        testSuite.testUnitDiesWithExactDamage();
        testSuite.testZeroDamage();
        testSuite.testCustomStats();
        testSuite.testHealToFull();
        testSuite.testHealthRatio();
        testSuite.testNegativeDamageIgnored();

        System.out.println("\n========================================");
        System.out.println("  Résumé des tests");
        System.out.println("========================================");
        System.out.println("Total : " + testsRun);
        System.out.println("✓ Réussis : " + testsPassed);
        System.out.println("✗ Échoués : " + testsFailed);
        System.out.println("========================================\n");

        if (testsFailed == 0) {
            System.out.println("✓ Tous les tests sont passés avec succès!");
            System.exit(0);
        } else {
            System.out.println("✗ Des tests ont échoué!");
            System.exit(1);
        }
    }

    private void setUp() {
        // Initialiser une nouvelle unité de combat avant chaque test
        combatUnit = new CombatUnit();
    }

    /**
     * Test 1 : Vérifier que la santé initiale est correcte
     */
    public void testInitialHealth() {
        setUp();
        runTest("testInitialHealth", () -> {
            assertEquals("La santé initiale doit être 100", CombatUnit.BASE_HEALTH, combatUnit.getHealth());
        });
    }

    /**
     * Test 2 : Vérifier que la puissance d'attaque initiale est correcte
     */
    public void testInitialAttackPower() {
        setUp();
        runTest("testInitialAttackPower", () -> {
            assertEquals("La puissance d'attaque initiale doit être 20", CombatUnit.BASE_ATTACK_POWER, combatUnit.getAttackPower());
        });
    }

    /**
     * Test 3 : Vérifier que l'unité est vivante au départ
     */
    public void testIsAliveAtStart() {
        setUp();
        runTest("testIsAliveAtStart", () -> {
            assertTrue("L'unité doit être vivante au départ", combatUnit.isAlive());
        });
    }

    /**
     * Test 4 : Vérifier que recevoir des dégâts réduit la santé correctement
     */
    public void testReceiveDamage() {
        setUp();
        runTest("testReceiveDamage", () -> {
            combatUnit.receiveDamage(30);
            assertEquals("La santé doit être réduite de 30", 70, combatUnit.getHealth());
        });
    }

    /**
     * Test 5 : Vérifier que multiple dégâts accumulent correctement
     */
    public void testMultipleDamage() {
        setUp();
        runTest("testMultipleDamage", () -> {
            combatUnit.receiveDamage(20);
            assertEquals("Après 20 dégâts, la santé doit être 80", 80, combatUnit.getHealth());
            
            combatUnit.receiveDamage(30);
            assertEquals("Après 30 dégâts supplémentaires, la santé doit être 50", 50, combatUnit.getHealth());
        });
    }

    /**
     * Test 6 : Vérifier que l'unité meurt quand la santé atteint 0
     */
    public void testUnitDeath() {
        setUp();
        runTest("testUnitDeath", () -> {
            combatUnit.receiveDamage(100);
            assertEquals("La santé doit être 0", 0, combatUnit.getHealth());
            assertTrue("L'unité doit être morte", !combatUnit.isAlive());
        });
    }

    /**
     * Test 7 : Vérifier que la santé ne peut pas devenir négative
     */
    public void testHealthNotNegative() {
        setUp();
        runTest("testHealthNotNegative", () -> {
            combatUnit.receiveDamage(150); // Dégâts supérieurs à la santé max
            assertEquals("La santé doit être limitée à 0", 0, combatUnit.getHealth());
            assertTrue("L'unité doit être morte", !combatUnit.isAlive());
        });
    }

    /**
     * Test 8 : Vérifier que l'unité reste vive avec santé > 0
     */
    public void testUnitStaysAliveWithHealth() {
        setUp();
        runTest("testUnitStaysAliveWithHealth", () -> {
            combatUnit.receiveDamage(99);
            assertEquals("La santé doit être 1", 1, combatUnit.getHealth());
            assertTrue("L'unité doit être vivante avec 1 pv", combatUnit.isAlive());
        });
    }

    /**
     * Test 9 : Vérifier le cas limite : 1 degat de plus tue l'unité
     */
    public void testUnitDiesWithExactDamage() {
        setUp();
        runTest("testUnitDiesWithExactDamage", () -> {
            combatUnit.receiveDamage(99);
            assertTrue("L'unité doit être vivante avec 1 pv", combatUnit.isAlive());
            
            combatUnit.receiveDamage(1);
            assertEquals("La santé doit être 0", 0, combatUnit.getHealth());
            assertTrue("L'unité doit être morte après ce dernier dégât", !combatUnit.isAlive());
        });
    }

    /**
     * Test 10 : Vérifier que les dégâts de 0 ne changent rien
     */
    public void testZeroDamage() {
        setUp();
        runTest("testZeroDamage", () -> {
            int healthBefore = combatUnit.getHealth();
            combatUnit.receiveDamage(0);
            assertEquals("La santé ne doit pas changer avec 0 dégât", healthBefore, combatUnit.getHealth());
        });
    }

    /**
     * Test 11 : Vérifier qu'une unité spécialisée garde bien ses statistiques propres
     */
    public void testCustomStats() {
        runTest("testCustomStats", () -> {
            CombatUnit customUnit = new CombatUnit(60, 12);
            assertEquals("La santé max doit être 60", 60, customUnit.getMaxHealth());
            assertEquals("La santé initiale doit suivre la santé max", 60, customUnit.getHealth());
            assertEquals("La puissance d'attaque doit être 12", 12, customUnit.getAttackPower());
        });
    }

    /**
     * Test 12 : Vérifier qu'une remise à zéro restaure complètement la vie
     */
    public void testHealToFull() {
        setUp();
        runTest("testHealToFull", () -> {
            combatUnit.receiveDamage(35);
            assertEquals("La santé doit avoir baissé", 65, combatUnit.getHealth());
            combatUnit.healToFull();
            assertEquals("La santé doit revenir au maximum", CombatUnit.BASE_HEALTH, combatUnit.getHealth());
        });
    }

    /**
     * Test 13 : Vérifier que le ratio de vie reste cohérent
     */
    public void testHealthRatio() {
        setUp();
        runTest("testHealthRatio", () -> {
            combatUnit.receiveDamage(25);
            assertDoubleEquals("Le ratio doit être de 0.75", 0.75, combatUnit.getHealthRatio(), 0.0001);
        });
    }

    /**
     * Test 14 : Vérifier qu'un dégât négatif est ignoré
     */
    public void testNegativeDamageIgnored() {
        setUp();
        runTest("testNegativeDamageIgnored", () -> {
            combatUnit.receiveDamage(-10);
            assertEquals("Un dégât négatif ne doit rien changer", CombatUnit.BASE_HEALTH, combatUnit.getHealth());
        });
    }

    // ===== Helpers =====

    private static void runTest(String testName, Runnable test) {
        testsRun++;
        try {
            test.run();
            System.out.println("✓ " + testName + " - RÉUSSI");
            testsPassed++;
        } catch (AssertionError e) {
            System.out.println("✗ " + testName + " - ÉCHOUÉ");
            System.out.println("  Erreur : " + e.getMessage());
            testsFailed++;
        } catch (Exception e) {
            System.out.println("✗ " + testName + " - ERREUR");
            System.out.println("  Exception : " + e.getMessage());
            testsFailed++;
        }
    }

    private static void assertEquals(String message, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(message + " [attendu: " + expected + ", obtenu: " + actual + "]");
        }
    }

    private static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertDoubleEquals(String message, double expected, double actual, double tolerance) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(message + " [attendu: " + expected + ", obtenu: " + actual + "]");
        }
    }
}
