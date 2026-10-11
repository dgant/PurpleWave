package Information.Geography.Calculations

import scala.collection.mutable

/** Contracts BWEM subdivisions before bases or geographical caches are built. */
private[Geography] object MergeMainZones {
  private val broadConnectionPixels = 32 * 8

  def apply[A](
    regions: Vector[A],
    mains: Set[A],
    bases: Set[A],
    altitude: A => Int,
    chokes: Vector[(A, A, Double)]): Map[A, A] = {

    val owner = mutable.Map(regions.map(r => r -> r): _*)
    val adjacent = regions.map(r => r -> chokes.collect {
      case (a, b, width) if a == r && b != r => (b, width)
      case (a, b, width) if b == r && a != r => (a, width)
    }).toMap
    var changed = true
    while (changed) {
      // Decide a whole round before assigning owners, so region iteration order cannot matter.
      val additions = regions.filter(r => owner(r) == r && ! bases(r) && ! mains(r)).flatMap(r => {
        val links = adjacent(r).map { case (other, width) => (owner(other), width) }
        val neighboringMains = links.map(_._1).filter(mains).distinct
        if (neighboringMains.size != 1) None else {
          val main = neighboringMains.head
          val (inside, outside) = links.partition(_._1 == main)
          // An enclosed back pocket belongs to the main even behind a mineral-blocked choke.
          // A passage must have a broad join, and cannot be a junction into the rest of the map.
          val enclosed = outside.isEmpty
          val passage = outside.map(_._1).distinct.size == 1 && inside.exists(_._2 >= broadConnectionPixels)
          if (altitude(r) == altitude(main) && (enclosed || passage)) Some(r -> main) else None
        }
      })
      additions.foreach { case (region, main) => owner(region) = main }
      changed = additions.nonEmpty
    }
    owner.toMap
  }
}
