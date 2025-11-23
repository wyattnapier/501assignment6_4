package com.example.assign6_4

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null

    private var _gx by mutableStateOf(0f)
    private var _gy by mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                SimpleGyroMaze(gx = _gx, gy = _gy)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            _gx = it.values[1]   // horizontal tilt
            _gy = it.values[0]   // vertical tilt
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun SimpleGyroMaze(gx: Float, gy: Float) {
    var ballX by remember { mutableStateOf(200f) }
    var ballY by remember { mutableStateOf(200f) }
    val ballRadius = 30f

    // walls
    val walls = listOf(
        Rect(100f, 100f, 400f, 120f),
        Rect(100f, 100f, 120f, 400f),
        Rect(100f, 400f, 400f, 420f),
        Rect(400f, 100f, 420f, 420f),
        Rect(200f, 200f, 300f, 220f)
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val nextX = ballX + gx * 5
        val nextY = ballY + gy * 5

        // check collisions
        var newX = nextX
        var newY = nextY

        walls.forEach { wall ->
            if (circleRectCollision(nextX, ballY, ballRadius, wall)) {
                newX = ballX // undo horizontal move
            }
            if (circleRectCollision(ballX, nextY, ballRadius, wall)) {
                newY = ballY // undo vertical move
            }
        }

        // update ball position
        ballX = newX.coerceIn(ballRadius, size.width - ballRadius)
        ballY = newY.coerceIn(ballRadius, size.height - ballRadius)

        // draw walls
        walls.forEach { drawRect(Color.Black, it.topLeft, it.size) }

        // draw ball
        drawCircle(Color.Red, radius = ballRadius, center = Offset(ballX, ballY))
    }
}

// simple circle vs rectangle collision
private fun circleRectCollision(cx: Float, cy: Float, radius: Float, rect: Rect): Boolean {
    val closestX = clamp(cx, rect.left, rect.right)
    val closestY = clamp(cy, rect.top, rect.bottom)
    val dx = cx - closestX
    val dy = cy - closestY
    return dx * dx + dy * dy < radius * radius
}

private fun clamp(value: Float, min: Float, max: Float): Float {
    return max(min, kotlin.math.min(value, max))
}