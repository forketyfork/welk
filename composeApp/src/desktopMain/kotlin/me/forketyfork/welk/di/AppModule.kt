package me.forketyfork.welk.di

import me.forketyfork.welk.domain.CardRepository
import me.forketyfork.welk.domain.DeckRepository
import me.forketyfork.welk.domain.FirestoreRepository
import me.forketyfork.welk.getPlatform
import me.forketyfork.welk.service.auth.AuthService
import me.forketyfork.welk.service.auth.FirestoreAuthService
import me.forketyfork.welk.vm.*
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule =
    module {

        singleOf(::FirestoreRepository) { bind<CardRepository>() }
        singleOf(::FirestoreRepository) { bind<DeckRepository>() }
        singleOf(::DesktopCardAnimationManager) { bind<CardAnimationManager>() }
        singleOf(::FirestoreAuthService) { bind<AuthService>() }
        singleOf(::DefaultCardInteractionManager) { bind<CardInteractionManager>() }

        viewModelOf(::DesktopCardViewModel)
        viewModelOf(::DesktopLoginViewModel)
        viewModelOf(::ThemeViewModel)

        factory { getPlatform() }
    }
