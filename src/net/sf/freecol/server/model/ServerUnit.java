
package net.sf.freecol.server.model;

import net.sf.freecol.common.model.*;
import net.sf.freecol.common.networking.Message;
import net.sf.freecol.server.networking.DummyConnection;

import org.w3c.dom.Element;


/**
* A Player with extra server data, e.g. AI data.
*/
public final class ServerUnit extends Unit {
    public static final String  COPYRIGHT = "Copyright (C) 2003 The FreeCol Team";
    public static final String  LICENSE = "http://www.gnu.org/licenses/gpl.html";
    public static final String  REVISION = "$Revision$";
    
    public static final int MISSION_STAND = 0,
                            MISSION_WANDER = 1,
                            MISSION_EXPLORE = 2;
    
    /** What we want this unit to do. */
    private int mission;
    
    /** Where to send our actions. */
    private DummyConnection connection;


    /**
    * Initiate a new <code>ServerUnit</code> of a specified type with the state set
    * to {@link #ACTIVE} if a carrier and {@link #SENTRY} otherwise. The
    * {@link Location} is set to <i>null</i>.
    *
    * @param The <code>Game</code> in which this <code>Unit</code> belong.
    * @param owner The Player owning the unit.
    * @param type The type of the unit.
    * @param connection The connection to send data through.
    */

    public ServerUnit(Game game, Player owner, int type, DummyConnection connection) {
        this(game, null, owner, type, isCarrier(type)?ACTIVE:SENTRY, connection);
    }

    /**
    * Initiate a new <code>Unit</code> with the specified parameters.
    *
    * @param The <code>Game</code> in which this <code>Unit</code> belong.
    * @param location The <code>Location/code> to place this <code>Unit</code> upon.
    * @param owner The <code>Player</code> owning this unit.
    * @param type The type of the unit.
    * @param s The initial state for this Unit (one of {@link #ACTIVE}, {@link #FORTIFY}...).
    * @param connection The connection to send data through.
    */
    public ServerUnit(Game game, Location location, Player owner, int type, int s, DummyConnection connection) {
        super(game, location, owner, type, s);
        
        this.connection = connection;
    }
    
    /**
    * Gets a mission for this ServerUnit.
    * @return One of MISSION_STAND, MISSION_WANDER, etc.
    */
    public int getMission() {
        return mission;
    }
    
    /**
    * Sets a mission for this ServerUnit.
    * @param mission One of MISSION_STAND, MISSION_WANDER, etc.
    */
    public void setMission(int mission) {
        this.mission = mission;
    }
    
    /**
    * Carries out this unit's mission. May send data through its connection.
    */
    public void doMission() {
        switch(mission) {
            case MISSION_STAND:
                return;
            case MISSION_EXPLORE: //TODO: Make this something other than wandering.
            case MISSION_WANDER:
                int i = getMovesLeft();
                int j = 0;
                Tile thisTile = this.getTile();
                while(i > 0) {
                    int direction = (int) Math.random() * 8;
                    for (j = 8; j > 0 && ((getGame().getMap().getNeighbourOrNull(direction, thisTile) == null) || (getMoveType(direction) != MOVE)); j--) {
                        direction = (int) Math.random() * 8;
                    }
                    if (j == 0) break;
                    thisTile = getGame().getMap().getNeighbourOrNull(direction, thisTile);
                    i -= 1; //TODO: account for different movement costs
                    // The server side does this -sjm
                    //move(direction);
                    Element moveElement = Message.createNewRootElement("move");
                    moveElement.setAttribute("unit", this.getID());
                    moveElement.setAttribute("direction", Integer.toString(direction));
                    connection.handleAndSendReply(moveElement);
                }
                return;
        }
    }
}
