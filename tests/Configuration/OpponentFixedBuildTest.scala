package Configuration

import Lifecycle.Configure.ConfigurationLoader.opponentFixedBuild

object OpponentFixedBuildTest {
  def main(args: Array[String]): Unit = {
    val policy = """{"Pluto":"PvRReaver","Pluto12":"PvRReaver"}"""
    assert(opponentFixedBuild("", "Pluto", "Default") == "Default")
    assert(opponentFixedBuild(policy, "Pluto", "Default") == "PvRReaver")
    assert(opponentFixedBuild(policy, "pLuTo12", "Default") == "PvRReaver")
    assert(opponentFixedBuild(policy, "Plutonium", "Default") == "Default")
    assert(opponentFixedBuild(policy, "Pluto1", "Default") == "Default")
    assert(opponentFixedBuild(policy, "Stardust", "") == "")
    println("Opponent fixed build: 6 checks passed")
  }
}
