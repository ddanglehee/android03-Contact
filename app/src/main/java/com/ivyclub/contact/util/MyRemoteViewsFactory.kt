package com.ivyclub.contact.util

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.ivyclub.contact.R
import com.ivyclub.data.ContactRepository
import com.ivyclub.data.model.SimplePlanData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.sql.Date

class MyRemoteViewsFactory(
    private val context: Context,
    private val repository: ContactRepository
) : RemoteViewsService.RemoteViewsFactory {

    private var data = emptyList<SimplePlanData>()

    private val refreshingJob: Job =
        CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
            getPlanListForWidget()
        }

    private suspend fun getPlanListForWidget() {
        val current = Date(System.currentTimeMillis())

        repository.loadPlanListWithFlow().collect { newPlanList ->
            val tmpList = newPlanList.filter {
                it.date.getExactYear() == current.getExactYear() &&
                        it.date.getExactMonth() == current.getExactMonth()
            }

            if (data != tmpList) {
                data = tmpList
                WidgetProvider.sendRefreshBroadcast(context)
            }
        }
    }

    override fun onCreate() {}

    override fun onDataSetChanged() {
        if (!refreshingJob.isActive) refreshingJob.start()
    }

    override fun onDestroy() {}

    override fun getCount() = data.size

    override fun getViewAt(position: Int): RemoteViews {
        val listviewWidget = RemoteViews(context.packageName, R.layout.item_widget)
        val planDate = data[position].date
        val dateText = String.format(
            context.getString(R.string.format_date_day),
            planDate.getDayOfMonth(),
            planDate.getDayOfWeek().korean
        )
        listviewWidget.setTextViewText(
            R.id.tv_widget_plan_date,
            dateText
        )
        listviewWidget.setTextViewText(R.id.tv_widget_plan_title, data[position].title)
        return listviewWidget
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun hasStableIds(): Boolean {
        return false
    }
}