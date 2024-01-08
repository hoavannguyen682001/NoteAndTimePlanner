package com.hoanv.notetimeplanner.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hoanv.notetimeplanner.ui.main.calendar.CalendarFragment
import com.hoanv.notetimeplanner.ui.main.person.PersonFragment
import com.hoanv.notetimeplanner.ui.main.home.dashboard.DashBoardFragment
import com.hoanv.notetimeplanner.ui.main.home.list.TasksFragment

class MainPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    private val listFragment: Map<Int, () -> Fragment> = mapOf(
        TASK to { TasksFragment() },
        CALENDAR to { CalendarFragment() },
        DASHBOARD to { DashBoardFragment() },
        PERSON to { PersonFragment() },
    )

    override fun getItemCount(): Int = listFragment.size

    override fun createFragment(position: Int): Fragment {
        return listFragment[position]?.invoke() ?: throw IndexOutOfBoundsException("")
    }

    companion object {
        private const val TASK = 0
        private const val CALENDAR = 1
        private const val DASHBOARD = 2
        private const val PERSON = 3
    }
}
