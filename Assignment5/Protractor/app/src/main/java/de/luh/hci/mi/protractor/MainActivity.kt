package de.luh.hci.mi.protractor

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {

    private var sketchView: SketchView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sketchView = SketchView(this)
        setContentView(sketchView)
        sketchView!!.requestFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "Train gestures") // group, id, order, title
        menu.add(1, 2, 1, "Test gestures - new Participant")
        return true // return true to enable menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> sketchView!!.trainGestures()
            2 -> sketchView!!.testGesturesWithNewParticipant()
        }
        return super.onOptionsItemSelected(item)
    }

}