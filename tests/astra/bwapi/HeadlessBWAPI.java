package bwapi;

/** In-memory BWAPI data for regression tests; never connects to a game. */
public final class HeadlessBWAPI {
    private static final java.util.Map<Integer, UnitCommand> commands = new java.util.HashMap<>();

    public static UnitCommand command(Unit unit) { return commands.get(unit.getID()); }

    public static void storm(Game game, int x, int y) {
        ClientData data = memory(ClientData.BulletData.SIZE);
        ClientData.BulletData state = data.new BulletData(0);
        state.setExists(true);
        state.setType(BulletType.Psionic_Storm.id);
        state.setPositionX(x);
        state.setPositionY(y);
        try {
            java.lang.reflect.Field field = Game.class.getDeclaredField("bullets");
            field.setAccessible(true);
            field.set(game, new Bullet[] {new Bullet(state, 0, game)});
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
    }

    private static ClientData memory(int size) {
        ClientData data = new ClientData();
        data.setBuffer(new WrappedBuffer(size));
        return data;
    }

    public static Player player(int id, Race race) {
        ClientData data = memory(ClientData.PlayerData.SIZE);
        ClientData.PlayerData state = data.new PlayerData(0);
        state.setName("Test player " + id);
        state.setRace(race.id);
        state.setType(PlayerType.Computer.id);
        return new Player(state, id, null) {
            @Override public boolean isNeutral() { return false; }
            @Override public boolean isAlly(Player other) { return other.getID() == id; }
            @Override public boolean isEnemy(Player other) { return other.getID() != id; }
            @Override public int getUpgradeLevel(UpgradeType upgrade) { return 0; }
            @Override public boolean hasResearched(TechType tech) { return false; }
        };
    }

    public static Game game(Player self) {
        Game game = new Game();
        game.botClientData().setBuffer(new WrappedBuffer(ClientData.GameData.SIZE));
        try {
            java.lang.reflect.Field field = Game.class.getDeclaredField("self");
            field.setAccessible(true);
            field.set(game, self);
            field = Game.class.getDeclaredField("playerSet");
            field.setAccessible(true);
            field.set(game, java.util.Collections.singletonList(self));
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
        game.setLatCom(false);
        return game;
    }

    public static Unit unit(Game game, int id, UnitType type, int x, int y) {
        ClientData data = memory(ClientData.UnitData.SIZE);
        ClientData.UnitData state = data.new UnitData(0);
        state.setType(type.id);
        state.setPositionX(x);
        state.setPositionY(y);
        state.setHitPoints(type.maxHitPoints());
        state.setShields(type.maxShields());
        return new Unit(state, id, game) {
            @Override public boolean issueCommand(UnitCommand command) {
                commands.put(getID(), command);
                return true;
            }
        };
    }
}
