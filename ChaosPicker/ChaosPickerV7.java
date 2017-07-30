import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import java.io.IOException;
import java.util.List;
import org.osbot.rs07.api.GroundItems;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.model.Character;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.utility.Condition;
import org.osbot.rs07.api.DoorHandler;
import org.osbot.rs07.api.Objects;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.api.Combat;
import org.osbot.rs07.api.Worlds;
import java.awt.*;


@ScriptManifest(author = "here", info = "My Trying to Learn", name = "Chaos picker V7", version = 0, logo = "")
public class ChaosPickerV7 extends Script { 
    
    @Override
    public void onStart() {
        log("You do all the cool intialization stuff that isnt looped here?");
    }
    
    private State getState() { //gets the state
        
        if (inventory.isEmptyExcept("Chaos rune", "Jug" ) || inventory.getItem("Chaos rune").getAmount() >= 100)
        {
            return State.BANK;
        }
  
        Area Fortress = new Area(3020,3623,3038,3640); //make this accurate, maybe just that room
        Position botPosition = new Position(myPlayer().getPosition());
        if (Fortress.contains(botPosition) == false) {
            return State.NOTTHERE;
        }
        
        GroundItem ChaosRune = getGroundItems().closestThatContains("Chaos rune");
        if (ChaosRune != null)  {
            return State.PICK;
        } 
        
        if (combat.isFighting() == true) {
            return State.COMBAT;
        }
        
            //if (guy nearby) return Player and world hop away || if picked up a rune.
            
            //add ge feature to sell when you get 10k runes - 1m worth
 
        return State.HOP;
    }  
    
    @Override
    public int onLoop() throws InterruptedException {
        
        Position runePosition = new Position(3021, 3640,0);
        Position botPosition = new Position(myPlayer().getPosition());
        Area Fortress = new Area(3000,3600,3100,3700);
        
            switch (getState()) {
            case PICK:

                if (botPosition.equals(runePosition) == false) {
                        runToRune();
                }
                
                GroundItem ChaosRune = getGroundItems().closestThatContains("Chaos rune");
                if (ChaosRune!=null)  {
                    log("Picking Rune");
                    ChaosRune.interact("Take");
                    sleep(random(300,700));
                } 
                break;
               
            case NOTTHERE:
                toFortress(); 
                break;
                
            case COMBAT:
                healOrFlee();
                do {
                    sleep(random(200,300));
                } while(myPlayer().isAnimating() == true);   
                    
                break;
                
            case BANK:       
                
                if (Fortress.contains(botPosition) == true) {
                        exitingFortress();
                }

                Entity booth = objects.closest("Bank Booth");
                do {
                    runToEdge();
                    booth = objects.closest("Bank Booth");
                } while (booth == null);
                banking();
                break;
                
            case HOP:
                healOrFlee();
                worlds.hopToF2PWorld();
                sleep(random(3000,6000));
                /*int currentWorld = worlds.getCurrentWorld();
                int i = worldSearch(currentWorld);
                log(i); 
                int worldsArray[] = {1,26,35,82,83,84,93,94};
                if (myPlayer().isUnderAttack() == true) {
                    healOrFlee();
                    break;
                } else {
                    log("tryna hop");
                    worlds.hop(worldsArray[i]);  
                    log(worldsArray[i]);
                }*/
                break;
        }
        return random(200, 300);
    }
    
    public static int worldSearch(int currentWorld) {
        int worldsArray[] = {1,26,35,82,83,84,93,94};
        for (int j = 0; j < 8; j++) {
            if (currentWorld == worldsArray[j]) {
                if (j == 7) {
                    int index = 0;
                    return index;
                } else {
                    int index = j + 1;
                    return index;
                }                
            }
        }
        return 3;  //if not found for some reason just go back to world 1
    }
    
    private void healCheck() throws InterruptedException {
        if(myPlayer().getHealthPercent() <=50) {
            if (inventory.getItem("Jug of wine") != null) {
                inventory.getItem("Jug of wine").interact("Drink");
                log("Healing Up");
                while (myPlayer().getAnimation() == 829)  { //drinking animation
                    sleep(250);
                }
            } 
        }
    }
    
    private void healOrFlee() throws InterruptedException {
        Area Fortress = new Area(3000,3600,3100,3700);
        Position botPosition = new Position(myPlayer().getPosition());
        if(myPlayer().getHealthPercent() <=50) {
            if (inventory.getItem("Jug of wine") != null) {
                inventory.getItem("Jug of wine").interact("Drink");
                log("Healing Up");
                while (myPlayer().getAnimation() == 829)  { //drinking animation
                    sleep(random(200,300));
                }
            }   else {
                log("Running away");
                if (Fortress.contains(botPosition)) { //failed to add check which room in the fortress and run from there
                        exitingFortress();
                    }
                runToEdge();
            }
        }
    }
    
    private void runToRune() throws InterruptedException {
        Position runePosition = new Position(3021, 3640,0);
        Position botPosition = new Position(myPlayer().getPosition());
        
        WebWalkEvent runToRune = new WebWalkEvent(runePosition);
        runToRune.setEnergyThreshold(20);
        
        runToRune.setBreakCondition(new Condition() { //conditions break on a returned true value
            @Override
            public boolean evaluate() {
                boolean dying = false;
                if (myPlayer().getHealthPercent() <=50) {
                    dying = true;
                } else {
                    dying = false;
                }
                return dying;
            }
        });
        log("Running to rune");
        execute(runToRune);
        healOrFlee();
    }
    
    private void runToEdge() throws InterruptedException {
        Position Edgeville = new Position(3093,3493,0);
        
        WebWalkEvent runToEdge = new WebWalkEvent(Edgeville);
        runToEdge.setEnergyThreshold(20);
        
        runToEdge.setBreakCondition(new Condition() {
            @Override
            public boolean evaluate() {
                boolean dying = false;
                if (myPlayer().getHealthPercent() <=50 && inventory.getItem("Jug of wine") != null) {
                    dying = true;
                } else {
                    dying = false;
                }
                return dying;
            }
        });
        log("running to edgeville");
        execute(runToEdge);
        healCheck();
    }
    
    private void banking() throws InterruptedException {
        log("banking");
        Entity booth = objects.closest("Bank Booth");
        booth.interact("Bank");
        sleep(1000);
        bank.depositAll();
        bank.depositWornItems();
        bank.withdraw("Chaos rune", 1); //you get a null point exception at the getState otherwise
        bank.withdraw("Steel scimitar", 1);
        sleep(1000);
        bank.close();
        sleep(1000);
        inventory.getItem("Steel scimitar").interact("Wield");
        sleep(1000);
        booth.interact("Bank");
        sleep(1000);
        bank.withdraw("Jug of wine",11);
        sleep(1000);
        bank.close();
    }
    
    private void toFortress() throws InterruptedException {
        Position fortress = new Position(3023,3628,0);
        WebWalkEvent runToFortress = new WebWalkEvent(fortress);
        runToFortress.setEnergyThreshold(20);
        
        runToFortress.setBreakCondition(new Condition() {
            @Override
            public boolean evaluate() {
                boolean dying = false;
                if (myPlayer().getHealthPercent() <=50) {
                    dying = true;
                } else {
                    dying = false;
                }
                return dying;
            }
        });
        log("Running to Fortress");
        execute(runToFortress);
        healOrFlee();
        
        log("walking to door1");
        WalkingEvent door1Walk = new WalkingEvent (new Position(3023, 3628, 0));
        door1Walk.setMinDistanceThreshold(0);
        sleep(2000);
        log("opening door1");
        Entity door1 = objects.closestThatContains(true,"Door");
        door1.interact("Open");
        sleep(2000);
        healOrFlee();
        
        WalkingEvent ladder1Walk = new WalkingEvent (new Position(3022, 3626, 0));
        ladder1Walk.setMinDistanceThreshold(0);
        sleep(2000);
        log("climbing ladder1");
        Entity ladder1 = objects.closestThatContains(true,"Ladder"); //realDistance - not pythag - is true
        ladder1.interact("Climb-up");
        sleep(2000);
        healOrFlee();
        
        log("walking to door2");
        WalkingEvent door2Walk = new WalkingEvent (new Position(3023, 3627, 1));
        door2Walk.setMinDistanceThreshold(0);
        sleep(2000);
        log("opening door2");
        Entity door2 = objects.closestThatContains(true,"Door");
        door2.interact("Open");
        sleep(2000);
        healOrFlee();
        
        log("walking to door3");
        WalkingEvent door3Walk = new WalkingEvent(new Position(3023, 3635, 0));
        door3Walk.setMinDistanceThreshold(0);
        execute(door3Walk);
        sleep(2000);
        log("opening door3");
        Entity door3 = objects.closestThatContains(true, "Door");
        door3.interact("Open");
        sleep(2000);
        healOrFlee();
        
        log("climbing down ladder");
        Entity ladder2 = objects.closestThatContains(true,"Ladder");
        ladder2.interact("Climb-down");
        sleep(2000);
        healOrFlee();
        
        combat.toggleAutoRetaliate(true);
        log("turned autoretaliate on");
    }
    
    private void exitingFortress() throws InterruptedException {
        combat.toggleAutoRetaliate(false);
        log("turned autoretaliate off");
        
        Position botPosition = new Position(myPlayer().getPosition());
        
        //bottom floor
        /*Area northWest = new Area(new Position(3020, 3636, 0), new Position(3024, 3641, 0));
        Area northCorridor = new Area(new Position(3025, 3636, 0), new Position(3033, 3639, 0));
        Area northEastCorner = new Area(new Position(3034, 3636, 0), new Position(3038, 3640, 0));
        Area EastCorridor = new Area(new Position(3034, 3628, 0), new Position(3036, 3635, 0)); 
        Area southEastCorner = new Area(new Position(3034,3623, 0), new Position(3038, 3627, 0));
        Area southCorridor = new Area(new Position(3025, 3625, 0), new Position(3033, 3627, 0)); 
        Area southWestCorner = new Area(new Position(3020, 3623, 0), new Position(3024, 3627, 0));
        Area westCorridor = new Area(new Position(3022, 3628, 0), new Position(3024, 3635, 0)); */
        
        //upstairs
        /*Area upNorthWestCorner = new Area(new Position(3020, 3636, 1), new Position(3024, 3641, 1));
        Area upNorthCorridor = new Area(new Position(3025, 3636, 1), new Position(3033, 3639, 1));
        Area upNorthEastCorner = new Area(new Position(3034, 3636, 1), new Position(3038, 3641, 1));
        Area upEastCorridor = new Area(new Position(3034, 3628, 1), new Position(3036, 3635, 1));
        Area upSouthEastCorner = new Area(new Position(3034, 3622, 1), new Position(3038, 3627, 1));
        Area upSouthCorridor = new Area(new Position(3025, 3625, 1), new Position(3033, 3627, 1));
        Area upSouthWestCorner = new Area(new Position(3020, 3622, 1), new Position(3024, 3627, 1));
        Area upWestCorridor = new Area(new Position(3022, 3628, 1), new Position(3024, 3635, 1)); */
        
        //if (northWest.contains(botPosition)) {
            WalkingEvent door1Walk = new WalkingEvent(new Position(3024, 3637, 0));
            door1Walk.setMinDistanceThreshold(0);
            execute(door1Walk);
            healCheck();
            sleep(1000);
            log("opening door");
            Entity door1 = objects.closestThatContains(true, "Door");
            door1.interact("Open");
            sleep(2000);
            
            log("walking to door2");
            WalkingEvent door2Walk = new WalkingEvent(new Position(3033, 3637, 0));
            door2Walk.setMinDistanceThreshold(0);
            execute(door2Walk);
            healCheck();
            sleep(2000);
        //}
        
        //if (northCorridor.contains(botPosition)) {
            log("opening door2");
            Entity door2 = objects.closestThatContains(true, "Door");
            door2.interact("Open");
            sleep(2000);
            
            log("walking to ladder1");
            WalkingEvent ladder1Walk = new WalkingEvent(new Position(3036, 3637, 0));
            ladder1Walk.setMinDistanceThreshold(0);
            execute(ladder1Walk);
            healCheck();
            sleep(2000);
        //}
        
        //if (northEastCorner.contains(botPosition)) {
            log("climbing ladder1");
            Entity ladder1 = objects.closestThatContains(true,"Ladder");
            ladder1.interact("Climb-up");
            sleep(2000);
            
            log("walking to door3");
            WalkingEvent door3Walk = new WalkingEvent(new Position(3034, 3637, 1));
            door3Walk.setMinDistanceThreshold(0);
            execute(door3Walk);
            healCheck();
            sleep(2000);
        //}
        
        //if (upNorthEastCorner.contains(botPosition)) {        
            log("opening door3");
            Entity door3 = objects.closestThatContains(true, "Door");
            door3.interact("Open");
            sleep(2000);
            
            log("walking to door4");
            WalkingEvent door4Walk = new WalkingEvent(new Position(3025, 3637, 1));
            door4Walk.setMinDistanceThreshold(0);
            execute(door4Walk);
            healCheck();
            sleep(2000);
        //}
        
        //if (upNorthCorridor.contains(botPosition)) {
            log("opening door4");
            Entity door4 = objects.closestThatContains(true, "Door");
            door4.interact("Open");
            sleep(2000);
            
            log("walking to door5");
            WalkingEvent door5Walk = new WalkingEvent(new Position(3023, 3636, 1));
            door5Walk.setMinDistanceThreshold(0);
            execute(door5Walk);
            healCheck();
            sleep(2000);
        //}

        //if (upNorthWestCorner.contains(botPosition)) {      
            log("opening door5");
            Entity door5 = objects.closestThatContains(true, "Door");
            door5.interact("Open");
            sleep(2000);
            
            log("Walking to door6");
            WalkingEvent door6Walk = new WalkingEvent(new Position(3023, 3628, 1));
            door6Walk.setMinDistanceThreshold(0);
            execute(door6Walk);
            healCheck();
            sleep(2000);
        //}

        //if (upWestCorridor.contains(botPosition)) {
            log("opening door6");
            Entity door6 = objects.closestThatContains(true, "Door");
            door6.interact("Open");
            sleep(2000);
            
            log("walking to ladder2");
            WalkingEvent ladder2Walk = new WalkingEvent(new Position(3022, 3626, 1));
            ladder2Walk.setMinDistanceThreshold(0);
            execute(ladder2Walk);
            healCheck();
            sleep(2000);
        //}
        
        //if (upSouthWestCorner.contains(botPosition)) {
            log("climbing ladder2");
            Entity ladder2 = objects.closestThatContains(true,"Ladder");
            ladder2.interact("Climb-down");
            sleep(2000);
        //}
        
        //if (southWestCorner.contains(botPosition)) {
            log("walking to door7");
            WalkingEvent door7Walk = new WalkingEvent(new Position(3023, 3627, 0));
            door7Walk.setMinDistanceThreshold(0);
            execute(door7Walk);
            healCheck();
            sleep(2000);
            
            log("opening door7");
            Entity door7 = objects.closestThatContains(true, "Door"); 
            door7.interact("Open");
            sleep(2000);
            
            getWalking().walk(new Position(3023, 3632, 0));
        //}
        
        //if (westCorridor.contains(botPosition)) { // the goal
            runToEdge();
        //}
    }
    
    @Override
        public void onExit() {
        log("bye");
    }



    private enum State {
        PICK, WAIT, BANK, COMBAT, HOP, NOTTHERE
    }
}
