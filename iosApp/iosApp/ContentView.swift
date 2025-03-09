import SwiftUI
import Shared

struct ContentView: View {
    @ObservedObject private(set) var viewModel = IosCardViewModel()
    
    var body: some View {
        CardView(currentCard: viewModel.sCurrentCard, isFlipped: viewModel.sIsFlipped)
            .frame(width: 315.0, height: 440.0, alignment: .center)
            .cornerRadius(10)
            .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(.black, lineWidth: 1)
                )
            .padding(20)
            .onAppear { viewModel.startObserving() }
            .onTapGesture {
                viewModel.processAction(action: CardAction.Flip())
            }.gesture(
                DragGesture()
                    .onEnded { value in
                        // Check horizontal direction
                        if value.translation.width > 0 {
                            viewModel.processAction(action: CardAction.SwipeRight())
                        } else if value.translation.width < 0 {
                            viewModel.processAction(action: CardAction.SwipeLeft())
                        }
                    }
            )
    }
}

extension ContentView {
    @MainActor
    class IosCardViewModel: CommonCardViewModel, ObservableObject {
        @Published var sCurrentCard: Card = Card(front: "", back: "")
        @Published var sIsFlipped: Bool = false

        init(repository: CardRepository = FirestoreRepository()) {
            super.init(repository: repository, cardAnimationManager: IosCardAnimationManager())
        }
        
        func startObserving() {
            // TODO what's the scope of these tasks? is there an easier way?
            Task {
                for await value in currentCard {
                    await MainActor.run {
                        self.sCurrentCard = value
                    }
                }
            }
            Task {
                for await value in isFlipped {
                    await MainActor.run {
                        self.sIsFlipped = value.boolValue
                    }
                }
            }
            Task {
                do {
                    try await nextCardOnAnimationCompletion()
                } catch {
                    print("Error: \(error)")
                }
            }
        }
    }
}

class IosCardAnimationManager: CommonCardAnimationManager {

    override func reset() {
        _animationCompleteTrigger.value = false
    }
    
    override func swipeLeft() {
        _animationCompleteTrigger.value = true
        // TODO animation
        Task {
            _animationCompleteTrigger.value = false
        }
    }
    
    override func swipeRight() {
        _animationCompleteTrigger.value = true
        // TODO animation
        Task {
            _animationCompleteTrigger.value = false
        }
    }
    
}

struct CardView: View {
    let currentCard: Card
    let isFlipped: Bool
    
    var body: some View {
        Text(currentCard.front)
        if (isFlipped) {
            Text(currentCard.back)
        }
    }
}
