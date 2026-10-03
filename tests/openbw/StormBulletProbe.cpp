// Integration probe for unmodified OpenBW/BWAPI. No PurpleWave or ladder game is started.
#include <algorithm>
#include <cstdlib>
#include <iostream>
#include <stdexcept>
#include <string>
#include <unordered_map>
#include <vector>

// Expose only the serializer's test seam; its compiled implementation is unmodified.
#define private public
#include "BWAPI/Server.h"
#undef private
#include "BWAPI/GameImpl.h"
#include "BWAPI/BulletImpl.h"
#include "BWAPI/PlayerImpl.h"
#include "BWAPI/UnitImpl.h"
#include <BWAPI.h>

static void require(bool condition, const std::string& message) {
  if (!condition) throw std::runtime_error(message);
}

struct ProbeModule : BWAPI::AIModule {
  bool complete;
  explicit ProbeModule(bool complete) : complete(complete) {}
  void onStart() override {
    if (complete) BWAPI::Broodwar->enableFlag(BWAPI::Flag::CompleteMapInformation);
  }
};

static BWAPI::Position findLocation(BWAPI::GameImpl& game) {
  for (int y = 16; y < game.mapHeight() - 16; ++y) {
    for (int x = 16; x < game.mapWidth() - 16; ++x) {
      BWAPI::Position center(x * 32 + 16, y * 32 + 16);
      bool clear = true;
      for (auto start : game.getStartLocations()) {
        if (center.getDistance(BWAPI::Position(start)) < 800) clear = false;
      }
      for (int dx : {-288, 0}) {
        for (int wx = -2; wx <= 2; ++wx) {
          for (int wy = -2; wy <= 2; ++wy) {
            if (!game.isWalkable((center.x + dx) / 8 + wx, center.y / 8 + wy)) clear = false;
          }
        }
      }
      if (clear) return center;
    }
  }
  throw std::runtime_error("No open test location far from starting bases");
}

int main(int argc, char** argv) {
  try {
    require(argc == 3, "usage: StormBulletProbe MAP MODE");
    const std::string mode = argv[2];
    const bool orphan = mode == "orphan";
    const bool hidden = mode == "hidden";
    const bool complete = mode == "complete";
    const bool enemyPerspective = mode == "enemy-hidden-caster";
    require(mode == "normal" || orphan || hidden || complete || enemyPerspective, "unknown mode");
    setenv("BWAPI_CONFIG_AUTO_MENU__AUTO_MENU", "SINGLE_PLAYER", 1);
    setenv("BWAPI_CONFIG_AUTO_MENU__MAP", argv[1], 1);
    setenv("BWAPI_CONFIG_AUTO_MENU__RACE", "Protoss", 1);
    setenv("BWAPI_CONFIG_AUTO_MENU__ENEMY_RACE", "Protoss", 1);
    setenv("BWAPI_CONFIG_AUTO_MENU__ENEMY_COUNT", "1", 1);
    setenv("BWAPI_CONFIG_AUTO_MENU__GAME_TYPE", "MELEE", 1);
    setenv("BWAPI_CONFIG_AUTO_MENU__AUTO_RESTART", "OFF", 1);
    setenv("OPENBW_ENABLE_UI", "0", 1);
    BW::GameOwner owner;
    owner.setPrintTextCallback([](const char*) {});
    BWAPI::BroodwarImpl_handle handle(owner.getGame());
    auto& game = *handle;
    ProbeModule module(complete);
    game.setAIModule(&module);
    game.autoMenuManager.startGame();

    BWAPI::Unit caster = nullptr;
    BWAPI::Unit observer = nullptr;
    BWAPI::Unit victim = nullptr;
    BWAPI::Player originalSelf = nullptr;
    BWAPI::Player opponent = nullptr;
    BWAPI::Position center;
    int firstStorm = -1;
    int lastStorm = -1;
    int stormId = -1;
    int visibleFrames = 0;
    int hiddenFrames = 0;
    int orphanFrames = 0;
    int damageFrames = 0;
    int underStormFrames = 0;
    int alternateVisibilityMismatches = 0;
    int targetPositionMismatches = 0;
    bool changed = false;
    bool expired = false;
    int priorHealth = 0;
    for (int step = 0; step < 260 && !game.bwgame.gameOver(); ++step) {
      game.update();
      const int frame = game.getFrameCount();
      // Cast across the engine's 100-frame fog refresh, so removing vision actually hides it.
      if (frame == 62) {
        game.setLocalSpeed(0);
        game.setLatCom(false);
        if (complete) require(game.isFlagEnabled(BWAPI::Flag::CompleteMapInformation), "complete-map flag not enabled");
        originalSelf = game.self();
        opponent = game.enemy();
        require(originalSelf && opponent, "two players required");
        center = findLocation(game);
        caster = game.createUnit(originalSelf, BWAPI::UnitTypes::Protoss_High_Templar, center - BWAPI::Position(288, 0));
        observer = game.createUnit(originalSelf, BWAPI::UnitTypes::Protoss_Observer, center);
        victim = game.createUnit(opponent, BWAPI::UnitTypes::Protoss_Zealot, center);
        require(caster && observer && victim, "failed to create probe units");
        caster->setEnergy(200);
        originalSelf->setResearched(BWAPI::TechTypes::Psionic_Storm, true);
      }
      if (frame == 70) {
        require(caster->useTech(BWAPI::TechTypes::Psionic_Storm, center), "storm command rejected: " + game.getLastError().toString());
      }
      if (firstStorm >= 0 && frame == firstStorm + 5) {
        if (orphan || hidden || complete) game.removeUnit(caster);
        if (hidden || complete) game.removeUnit(observer);
        if (enemyPerspective) {
          // Keep the simulation and real spell unchanged; inspect its report from the victim's POV.
          game.BWAPIPlayer = static_cast<BWAPI::PlayerImpl*>(opponent);
        }
        changed = true;
      }

      // Exercise the production serializer, not a reimplementation or a hand-built BulletData.
      // Upstream's external-client serializer calls the unimplemented getRandomSeed.
      // Leave it opt-in so that failure is reported separately from the module API probe.
      if (std::getenv("PROBE_SERIALIZER")) game.server.updateSharedMemory();
      for (int slot = 0; slot < 100; ++slot) {
        if (!std::getenv("PROBE_SERIALIZER")) break;
        const auto* api = game.getBulletFromIndex(slot);
        const auto& data = game.server.data->bullets[slot];
        require(data.exists == api->exists(), "serialized bullet existence mismatch");
        require(data.id == api->getID() && data.type == api->getType().getID(), "serialized bullet identity mismatch");
        require(data.positionX == api->getPosition().x && data.positionY == api->getPosition().y, "serialized bullet position mismatch");
        require(data.removeTimer == api->getRemoveTimer(), "serialized bullet lifetime mismatch");
      }
      int rawStorms = 0;
      for (auto it = game.bwgame.BulletNodeTable_begin(); it != game.bwgame.BulletNodeTable_end(); ++it) {
        const auto raw = *it;
        if (raw.type() != BWAPI::BulletTypes::Psionic_Storm.getID()) continue;
        ++rawStorms;
        if (firstStorm < 0) firstStorm = frame;
        lastStorm = frame;
        auto* api = game.getBulletFromIndex(raw.getIndex());
        require(game.getBullets().count(api) == 1, "active engine storm omitted from BWAPI bullet set");
        const BWAPI::TilePosition tile(center);
        const int selfIndex = static_cast<BWAPI::PlayerImpl*>(game.self())->getIndex();
        const bool tileVisible = !(game.bwgame.getActiveTile(tile.x, tile.y).bVisibilityFlags & (1 << selfIndex));
        require(game.isVisible(tile.x, tile.y) == tileVisible, "map visibility differs from engine mask");
        const bool visible = tileVisible || complete;
        require(api->exists() == visible && api->isVisible() == visible, "self visibility/existence mismatch");
        if (visible) {
          ++visibleFrames;
          require(api->getType() == BWAPI::BulletTypes::Psionic_Storm, "wrong storm bullet type");
          require(api->getPosition() == center, "wrong storm center");
          require(api->getTargetPosition() == BWAPI::Position(raw.targetPosition().x, raw.targetPosition().y),
                  "target position differs from engine waypoint");
          if (api->getTargetPosition() != center) ++targetPositionMismatches;
          require(api->getRemoveTimer() == raw.time_remaining(), "wrong remaining timer");
          require(api->getVelocityX() == 0 && api->getVelocityY() == 0, "stationary storm has velocity");
          if (stormId < 0) stormId = api->getID();
          require(api->getID() == stormId, "storm ID changed during lifetime");
          require(api->getTarget() == nullptr, "position-cast storm unexpectedly has a unit target");
          if (!api->getSource()) ++orphanFrames;
          if (orphan && changed && frame > firstStorm + 6) require(api->getSource() == nullptr, "dead caster still reported as source");
          if (enemyPerspective && changed && frame > firstStorm + 6) {
            require(api->getPlayer() == originalSelf, "enemy storm owner mismatch");
          }
        } else {
          ++hiddenFrames;
          require(api->getType() == BWAPI::BulletTypes::Unknown && api->getPosition() == BWAPI::Positions::Unknown,
                  "hidden storm leaks type or position");
        }
        if (!complete && !enemyPerspective) {
          const int enemyIndex = static_cast<BWAPI::PlayerImpl*>(opponent)->getIndex();
          const bool actuallyVisibleToEnemy = !(game.bwgame.getActiveTile(tile.x, tile.y).bVisibilityFlags & (1 << enemyIndex));
          if (api->isVisible(opponent) != actuallyVisibleToEnemy) ++alternateVisibilityMismatches;
        }
      }
      if (firstStorm >= 0 && rawStorms == 0) {
        expired = true;
        for (auto* bullet : game.getBullets()) require(bullet->getType() != BWAPI::BulletTypes::Psionic_Storm, "expired storm remains accessible");
      }
      if (victim && victim->exists()) {
        const bool engineUnderStorm = static_cast<BWAPI::UnitImpl*>(victim)->bwunit.stormTimer() != 0;
        require(victim->isUnderStorm() == engineUnderStorm, "unit storm flag mismatch");
        if (engineUnderStorm) ++underStormFrames;
        const int health = victim->getHitPoints() + victim->getShields();
        if (priorHealth > health && firstStorm >= 0) ++damageFrames;
        priorHealth = health;
      }
      game.bwgame.nextFrame();
    }
    require(firstStorm >= 0 && visibleFrames > (hidden ? 0 : 20) && expired, "did not observe a complete storm lifecycle");
    if (mode == "normal" || orphan || enemyPerspective) require(damageFrames > 0 && underStormFrames > 0, "storm did not damage/flag its victim");
    if (hidden) require(hiddenFrames > 0, "fog-of-war case never hid the storm");
    if (orphan || enemyPerspective) require(orphanFrames > 0, "missing-source case not reached");
    std::cout << "PASS mode=" << mode << " first=" << firstStorm << " last=" << lastStorm
              << " visible=" << visibleFrames << " hidden=" << hiddenFrames << " source_null=" << orphanFrames
              << " damage_frames=" << damageFrames << " under_storm_frames=" << underStormFrames
              << " target_position_not_center=" << targetPositionMismatches
              << " other_player_visibility_mismatches=" << alternateVisibilityMismatches << '\n';
    return 0;
  } catch (const std::exception& e) {
    std::cerr << "FAIL " << e.what() << '\n';
    return 1;
  }
}
