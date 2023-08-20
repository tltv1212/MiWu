package io.github.sky130.miwu.ui.miot.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.sky130.miwu.databinding.DeviceOutletDefaultBinding
import io.github.sky130.miwu.logic.dao.HomeDAO
import io.github.sky130.miwu.logic.model.miot.MiotService
import io.github.sky130.miwu.logic.network.MiotSpecService
import io.github.sky130.miwu.ui.manager.MiWidgetManager
import io.github.sky130.miwu.ui.miot.BaseFragment
import io.github.sky130.miwu.util.GlideUtils
import java.util.concurrent.Executors

class OutletDefaultFragment(private val miotServices: ArrayList<MiotService>) : BaseFragment() {

    private lateinit var binding: DeviceOutletDefaultBinding
    private val executor = Executors.newSingleThreadExecutor()
    private val manager = MiWidgetManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DeviceOutletDefaultBinding.inflate(inflater)
        executor.execute {
            val home = HomeDAO.getHome(HomeDAO.getHomeIndex()) // 获取家庭对象
            var url = ""
            home?.deviceList?.forEach { device ->
                if (device.model == getModel()) {
                    url = device.iconUrl
                }
            }
            if (url.isNotEmpty()) GlideUtils.loadImg(url, binding.deviceImage)
        }
        for (service in miotServices) { // 遍历服务
            val siid = service.iid // 获取当前服务的iid
            val serviceType = MiotSpecService.parseUrn(service.type)?.value ?: continue // 获取当前服务类型
            when (serviceType) {
                "switch" -> { // 开关类型
                    for (property in service.properties) { // 遍历属性
                        val piid = property.iid // 获取当前属性iid
                        val propertyType =
                            MiotSpecService.parseUrn(property.type)?.value ?: continue // 获取当前特性类型
                        when (propertyType) {
                            "on" -> { // on 代表的一般是开关
                                manager.addView(
                                    binding.switchOutlet,
                                    propertyType,
                                    siid,
                                    piid,
                                    false
                                )
                            }
                        }
                    }
                }
            }
        }
        manager.init()
        manager.update()
        return binding.root
    }
}