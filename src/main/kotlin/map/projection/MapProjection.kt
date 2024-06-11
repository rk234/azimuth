package map.projection

import org.joml.Vector2f

interface MapProjection {
    fun toCartesian(latitude: Float, longitude: Float): Vector2f
    fun fromCartesian(point: Vector2f): Pair<Float, Float>
}