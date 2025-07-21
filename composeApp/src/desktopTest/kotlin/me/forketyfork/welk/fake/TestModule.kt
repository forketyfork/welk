package me.forketyfork.welk.fake

import me.forketyfork.welk.domain.CardRepository
import me.forketyfork.welk.domain.DeckRepository
import me.forketyfork.welk.service.auth.AuthService
import me.forketyfork.welk.vm.CardAnimationManager
import me.forketyfork.welk.vm.CardInteractionManager
import me.forketyfork.welk.vm.DefaultCardInteractionManager
import me.forketyfork.welk.vm.DesktopCardAnimationManager
import me.forketyfork.welk.vm.DesktopCardViewModel
import me.forketyfork.welk.vm.DesktopLoginViewModel
import me.forketyfork.welk.vm.ThemeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val testAppModule: Module = module {
    singleOf(::FakeRepository) { bind<CardRepository>(); bind<DeckRepository>() }
    singleOf(::FakeAuthService) { bind<AuthService>() }
    singleOf(::DesktopCardAnimationManager) { bind<CardAnimationManager>() }
    singleOf(::DefaultCardInteractionManager) { bind<CardInteractionManager>() }

    viewModelOf(::DesktopCardViewModel)
    viewModelOf(::DesktopLoginViewModel)
    viewModelOf(::ThemeViewModel)

    factory { me.forketyfork.welk.getPlatform() }
}
