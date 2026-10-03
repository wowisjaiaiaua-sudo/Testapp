package com.example.game

import org.json.JSONArray
import org.json.JSONObject

object LevelTemplates {

    val OFFICIAL_LEVELS = listOf(
        LevelMetadata(
            id = "stereo_madness",
            title = "Stereo Madness",
            difficulty = "Easy",
            stars = 1,
            bpm = 130,
            bgColorHex = "#0D2B45",
            floorColorHex = "#203C56",
            accentColorHex = "#00F0FF",
            totalLength = 175f
        ),
        LevelMetadata(
            id = "back_on_track",
            title = "Back On Track",
            difficulty = "Normal",
            stars = 2,
            bpm = 135,
            bgColorHex = "#2B0B3F",
            floorColorHex = "#441261",
            accentColorHex = "#FF007F",
            totalLength = 205f
        ),
        LevelMetadata(
            id = "polargeist",
            title = "Polargeist",
            difficulty = "Hard",
            stars = 3,
            bpm = 140,
            bgColorHex = "#0B3F2E",
            floorColorHex = "#125E45",
            accentColorHex = "#00FF66",
            totalLength = 230f
        ),
        LevelMetadata(
            id = "dry_out",
            title = "Dry Out",
            difficulty = "Harder",
            stars = 4,
            bpm = 145,
            bgColorHex = "#3F1B0B",
            floorColorHex = "#5E2912",
            accentColorHex = "#FFB703",
            totalLength = 255f
        ),
        LevelMetadata(
            id = "jumper",
            title = "Jumper",
            difficulty = "Insane",
            stars = 5,
            bpm = 150,
            bgColorHex = "#1A0933",
            floorColorHex = "#2D1252",
            accentColorHex = "#7B2CBF",
            totalLength = 280f
        )
    )

    fun getLevelObjects(levelId: String): List<GameObject> {
        return when (levelId) {
            "stereo_madness" -> createStereoMadness()
            "back_on_track" -> createBackOnTrack()
            "polargeist" -> createPolargeist()
            "dry_out" -> createDryOut()
            "jumper" -> createJumper()
            else -> createStereoMadness()
        }
    }

    private fun createStereoMadness(): List<GameObject> {
        val list = mutableListOf<GameObject>()
        var id = 0

        // Single spikes to teach jumping
        list.add(GameObject(id++, 14f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 22f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 23f, 0f, type = ObstacleType.SPIKE)) // double spike

        // Step stair block
        list.add(GameObject(id++, 30f, 0f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 31f, 0f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 31f, 1f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 33f, 0f, type = ObstacleType.SPIKE))

        // Yellow Jump Pad
        list.add(GameObject(id++, 38f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 40f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 41f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 42f, 2f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 43f, 2f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 44f, 2f, type = ObstacleType.BLOCK))

        // Secret Coin 1 high up
        list.add(GameObject(id++, 43f, 4f, type = ObstacleType.COIN))

        // Yellow Jump Orb over spike pit
        list.add(GameObject(id++, 50f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 51f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 52f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 51f, 2f, type = ObstacleType.ORB_YELLOW))

        // Platform bridge
        list.add(GameObject(id++, 58f, 1f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 59f, 1f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 60f, 1f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 61f, 1f, type = ObstacleType.SPIKE)) // spike on platform!
        list.add(GameObject(id++, 62f, 1f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 63f, 1f, type = ObstacleType.BLOCK))

        // Drop and jump
        list.add(GameObject(id++, 70f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 71f, 0f, type = ObstacleType.SPIKE))

        // Triple spike test
        list.add(GameObject(id++, 80f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 81f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 82f, 0f, type = ObstacleType.SPIKE))

        // Stairway to sky
        list.add(GameObject(id++, 90f, 0f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 92f, 1f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 94f, 2f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 95f, 2f, type = ObstacleType.COIN))
        list.add(GameObject(id++, 96f, 3f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 97f, 3f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 99f, 0f, type = ObstacleType.SPIKE))

        // Orb combo
        list.add(GameObject(id++, 106f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 107f, 1.8f, type = ObstacleType.ORB_YELLOW))
        list.add(GameObject(id++, 112f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 113f, 1.8f, type = ObstacleType.ORB_YELLOW))
        list.add(GameObject(id++, 118f, 0f, type = ObstacleType.SPIKE))

        // Saw blade hazard
        list.add(GameObject(id++, 126f, 0f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 132f, 1f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 135f, 3f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 136f, 3f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 137f, 3f, type = ObstacleType.BLOCK))

        // Coin 3
        list.add(GameObject(id++, 145f, 1.5f, type = ObstacleType.COIN))
        list.add(GameObject(id++, 145f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 146f, 0f, type = ObstacleType.SPIKE))

        // Final dash
        list.add(GameObject(id++, 154f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 160f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 162f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 163f, 0f, type = ObstacleType.SPIKE))

        // Finish Line
        list.add(GameObject(id++, 170f, 0f, height = 5f, type = ObstacleType.FINISH))

        return list
    }

    private fun createBackOnTrack(): List<GameObject> {
        val list = mutableListOf<GameObject>()
        var id = 0

        list.add(GameObject(id++, 15f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 20f, 0f, type = ObstacleType.PAD_PURPLE))
        list.add(GameObject(id++, 23f, 1.5f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 24f, 1.5f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 27f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 28f, 0f, type = ObstacleType.SPIKE))

        // Jump pad onto elevated runway
        list.add(GameObject(id++, 36f, 0f, type = ObstacleType.PAD_YELLOW))
        for (x in 38..46) {
            list.add(GameObject(id++, x.toFloat(), 2.5f, type = ObstacleType.BLOCK))
        }
        list.add(GameObject(id++, 42f, 3.5f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 44f, 4.5f, type = ObstacleType.COIN))

        // Hanging spikes
        list.add(GameObject(id++, 54f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 55f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 55f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 56f, 1.8f, type = ObstacleType.ORB_YELLOW))

        // Step rhythm
        list.add(GameObject(id++, 65f, 0f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 67f, 1f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 69f, 2f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 70f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 71f, 0f, type = ObstacleType.SPIKE))

        // Double pad boost
        list.add(GameObject(id++, 80f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 85f, 3f, type = ObstacleType.PAD_PURPLE))
        list.add(GameObject(id++, 88f, 5f, type = ObstacleType.COIN))

        // Ground spikes with orb chain
        list.add(GameObject(id++, 96f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 97f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 98f, 2f, type = ObstacleType.ORB_PURPLE))
        list.add(GameObject(id++, 102f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 103f, 2f, type = ObstacleType.ORB_YELLOW))
        list.add(GameObject(id++, 107f, 0f, type = ObstacleType.SPIKE))

        // Saw blade gauntlet
        list.add(GameObject(id++, 118f, 0f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 124f, 1f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 125f, 1f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 126f, 1f, type = ObstacleType.BLOCK))

        // Platform hurdles
        for (i in 0..3) {
            val ox = 138f + (i * 8f)
            list.add(GameObject(id++, ox, 0f, type = ObstacleType.SPIKE))
            list.add(GameObject(id++, ox + 1f, 0f, type = ObstacleType.SPIKE))
            list.add(GameObject(id++, ox + 3f, 1.5f, type = ObstacleType.BLOCK))
        }

        // Final stretch
        list.add(GameObject(id++, 180f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 181f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 182f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 187f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 192f, 2f, type = ObstacleType.COIN))

        list.add(GameObject(id++, 200f, 0f, height = 5f, type = ObstacleType.FINISH))
        return list
    }

    private fun createPolargeist(): List<GameObject> {
        val list = mutableListOf<GameObject>()
        var id = 0

        list.add(GameObject(id++, 15f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 22f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 24f, 3f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 25f, 3f, type = ObstacleType.BLOCK))

        // Gravity Pad intro! Flips player to ceiling
        list.add(GameObject(id++, 35f, 0f, type = ObstacleType.PAD_GRAVITY))
        // Ceiling runway
        for (x in 37..52) {
            list.add(GameObject(id++, x.toFloat(), 6f, type = ObstacleType.BLOCK))
        }
        list.add(GameObject(id++, 42f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 46f, 4f, type = ObstacleType.COIN))
        list.add(GameObject(id++, 48f, 5f, type = ObstacleType.SPIKE_DOWN))

        // Flip back down with portal
        list.add(GameObject(id++, 53f, 4f, height = 3f, type = ObstacleType.PORTAL_GRAVITY_DOWN))

        // Ground saw blades
        list.add(GameObject(id++, 62f, 0f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 68f, 0f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 65f, 2f, type = ObstacleType.ORB_YELLOW))

        // Gravity Orb in mid-air!
        list.add(GameObject(id++, 78f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 79f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 79f, 2f, type = ObstacleType.ORB_GRAVITY))
        for (x in 81..92) {
            list.add(GameObject(id++, x.toFloat(), 6f, type = ObstacleType.BLOCK))
        }
        list.add(GameObject(id++, 85f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 93f, 4f, height = 3f, type = ObstacleType.PORTAL_GRAVITY_DOWN))

        // Fast rhythm sequence
        list.add(GameObject(id++, 102f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 105f, 2.5f, type = ObstacleType.ORB_YELLOW))
        list.add(GameObject(id++, 109f, 2.5f, type = ObstacleType.ORB_PURPLE))
        list.add(GameObject(id++, 113f, 2f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 114f, 2f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 117f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 118f, 0f, type = ObstacleType.SPIKE))

        // Secret coin 2 in mid air gap
        list.add(GameObject(id++, 130f, 3.5f, type = ObstacleType.COIN))
        list.add(GameObject(id++, 130f, 0f, type = ObstacleType.SPIKE))

        // More gravity flips
        list.add(GameObject(id++, 142f, 0f, type = ObstacleType.PAD_GRAVITY))
        for (x in 144..158) {
            list.add(GameObject(id++, x.toFloat(), 6f, type = ObstacleType.BLOCK))
        }
        list.add(GameObject(id++, 149f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 153f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 159f, 4f, height = 3f, type = ObstacleType.PORTAL_GRAVITY_DOWN))

        // Triple spike dash
        list.add(GameObject(id++, 170f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 171f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 172f, 0f, type = ObstacleType.SPIKE))

        list.add(GameObject(id++, 185f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 190f, 3f, type = ObstacleType.ORB_YELLOW))
        list.add(GameObject(id++, 195f, 3f, type = ObstacleType.COIN))

        list.add(GameObject(id++, 208f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 209f, 0f, type = ObstacleType.SPIKE))

        list.add(GameObject(id++, 220f, 0f, height = 5f, type = ObstacleType.FINISH))
        return list
    }

    private fun createDryOut(): List<GameObject> {
        val list = mutableListOf<GameObject>()
        var id = 0

        // Cube section
        list.add(GameObject(id++, 15f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 22f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 23f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 28f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 30f, 3f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 31f, 3f, type = ObstacleType.BLOCK))

        // Enter Ship Mode!
        list.add(GameObject(id++, 40f, 1f, height = 4f, type = ObstacleType.PORTAL_SHIP))

        // Ship Cavern with ceiling and floor obstacles
        for (x in 45..110 step 3) {
            // Ceiling spikes
            list.add(GameObject(id++, x.toFloat(), 7f, type = ObstacleType.SPIKE_DOWN))
        }
        // Floor spikes during ship
        list.add(GameObject(id++, 50f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 51f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 60f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 61f, 0f, type = ObstacleType.SPIKE))

        // Floating saw blades to fly around
        list.add(GameObject(id++, 65f, 3.5f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 75f, 2.0f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 85f, 4.5f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 75f, 5.5f, type = ObstacleType.COIN)) // high risk coin!

        list.add(GameObject(id++, 95f, 2.5f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 105f, 3.8f, type = ObstacleType.SAW_BLADE))

        // Exit Ship Mode -> Back to Cube!
        list.add(GameObject(id++, 115f, 1f, height = 4f, type = ObstacleType.PORTAL_CUBE))

        // Inverted Gravity Cube section
        list.add(GameObject(id++, 122f, 1f, height = 4f, type = ObstacleType.PORTAL_GRAVITY_UP))
        for (x in 124..170) {
            list.add(GameObject(id++, x.toFloat(), 6f, type = ObstacleType.BLOCK))
        }
        list.add(GameObject(id++, 132f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 140f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 141f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 148f, 3.5f, type = ObstacleType.ORB_YELLOW))
        list.add(GameObject(id++, 155f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 160f, 4f, type = ObstacleType.COIN))

        // Restore normal gravity
        list.add(GameObject(id++, 172f, 2f, height = 4f, type = ObstacleType.PORTAL_GRAVITY_DOWN))

        // Final Cube Sprint
        list.add(GameObject(id++, 182f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 186f, 3f, type = ObstacleType.ORB_PURPLE))
        list.add(GameObject(id++, 190f, 2f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 191f, 2f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 196f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 197f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 204f, 0f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 212f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 213f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 214f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 224f, 2.5f, type = ObstacleType.COIN))

        list.add(GameObject(id++, 245f, 0f, height = 5f, type = ObstacleType.FINISH))
        return list
    }

    private fun createJumper(): List<GameObject> {
        val list = mutableListOf<GameObject>()
        var id = 0

        // High octane 150 BPM jump sequences
        list.add(GameObject(id++, 12f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 18f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 21f, 2.5f, type = ObstacleType.ORB_YELLOW))
        list.add(GameObject(id++, 24f, 2.5f, type = ObstacleType.ORB_PURPLE))
        list.add(GameObject(id++, 27f, 1.5f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 28f, 1.5f, type = ObstacleType.BLOCK))

        // Spikes on floor
        list.add(GameObject(id++, 34f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 35f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 36f, 0f, type = ObstacleType.SPIKE))

        // Gravity flip combo
        list.add(GameObject(id++, 44f, 0f, type = ObstacleType.PAD_GRAVITY))
        for (x in 46..68) {
            list.add(GameObject(id++, x.toFloat(), 6f, type = ObstacleType.BLOCK))
        }
        list.add(GameObject(id++, 52f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 58f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 59f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 62f, 3.8f, type = ObstacleType.COIN))
        list.add(GameObject(id++, 69f, 3f, height = 3f, type = ObstacleType.PORTAL_GRAVITY_DOWN))

        // Fast Ship Flight
        list.add(GameObject(id++, 78f, 1f, height = 4f, type = ObstacleType.PORTAL_SHIP))
        for (x in 82..145 step 4) {
            list.add(GameObject(id++, x.toFloat(), 7f, type = ObstacleType.SPIKE_DOWN))
            list.add(GameObject(id++, (x + 2).toFloat(), 0f, type = ObstacleType.SPIKE))
        }
        list.add(GameObject(id++, 95f, 3.5f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 110f, 2.0f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 125f, 4.5f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 125f, 1.5f, type = ObstacleType.COIN))
        list.add(GameObject(id++, 138f, 3.0f, type = ObstacleType.SAW_BLADE))

        list.add(GameObject(id++, 150f, 1f, height = 4f, type = ObstacleType.PORTAL_CUBE))

        // Ultra orb bounce sequence
        list.add(GameObject(id++, 160f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 163f, 2.5f, type = ObstacleType.ORB_YELLOW))
        list.add(GameObject(id++, 166f, 3f, type = ObstacleType.ORB_GRAVITY))
        for (x in 168..185) {
            list.add(GameObject(id++, x.toFloat(), 6f, type = ObstacleType.BLOCK))
        }
        list.add(GameObject(id++, 174f, 5f, type = ObstacleType.SPIKE_DOWN))
        list.add(GameObject(id++, 186f, 3f, height = 3f, type = ObstacleType.PORTAL_GRAVITY_DOWN))

        // Grand Finale triple spikes and jumps
        list.add(GameObject(id++, 196f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 197f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 198f, 0f, type = ObstacleType.SPIKE))

        list.add(GameObject(id++, 208f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 212f, 3f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 213f, 3f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 214f, 3f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 215f, 3f, type = ObstacleType.BLOCK))
        list.add(GameObject(id++, 218f, 5f, type = ObstacleType.COIN))

        list.add(GameObject(id++, 230f, 0f, type = ObstacleType.SAW_BLADE))
        list.add(GameObject(id++, 236f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 237f, 0f, type = ObstacleType.SPIKE))
        list.add(GameObject(id++, 238f, 0f, type = ObstacleType.SPIKE))

        list.add(GameObject(id++, 255f, 0f, type = ObstacleType.PAD_YELLOW))
        list.add(GameObject(id++, 270f, 0f, height = 5f, type = ObstacleType.FINISH))
        return list
    }

    // JSON serialization for custom levels
    fun serializeObjects(objects: List<GameObject>): String {
        val array = JSONArray()
        for (obj in objects) {
            val json = JSONObject()
            json.put("x", obj.x)
            json.put("y", obj.y)
            json.put("type", obj.type.name)
            array.put(json)
        }
        return array.toString()
    }

    fun deserializeObjects(jsonString: String): List<GameObject> {
        val list = mutableListOf<GameObject>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val json = array.getJSONObject(i)
                val x = json.getDouble("x").toFloat()
                val y = json.getDouble("y").toFloat()
                val typeName = json.getString("type")
                val type = runCatching { ObstacleType.valueOf(typeName) }.getOrDefault(ObstacleType.BLOCK)
                list.add(GameObject(i, x, y, type = type))
            }
        } catch (_: Exception) {}
        return list
    }
}
