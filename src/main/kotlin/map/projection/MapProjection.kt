package map.projection

import org.joml.Vector2f
import java.util.Vector

interface MapProjection {
    fun toCartesian(latlon: Vector2f): Vector2f
    fun fromCartesian(point: Vector2f): Vector2f
}