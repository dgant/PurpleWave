package Types.Buildable

import bwapi.UpgradeType

class BuildableUpgrade(
  upgradeType: UpgradeType,
  upgradeLevel: Int = 1)
    extends Buildable(
      upgrade = Some(upgradeType),
      level = upgradeLevel) {}
