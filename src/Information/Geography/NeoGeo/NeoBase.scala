package Information.Geography.NeoGeo

/**
  * A Base is a Region which contains a cluster of resources.
  * Each Base has a Foundation, which is an optimal location to construct a Command Center, Nexus, or Hatchery.
  */
class NeoBase extends NeoRegion {
  private var _country: NeoCountry = _
  private var _metro: NeoMetro = _
  private var _natural: Option[NeoBase] = None
  private var _main: Option[NeoBase] = None
  private var _isStart: Boolean = false
}
