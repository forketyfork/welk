import SwiftUI
import Shared

struct ContentView: View {
    @ObservedObject private(set) var viewModel = IosCardViewModel()
    
    @State private var offset: CGSize = .zero
    @State private var isAnimating = false
    
    
    var body: some View {
        CardView(currentCard: viewModel.sCurrentCard, isFlipped: viewModel.sIsFlipped)
            .frame(width: 315.0, height: 440.0, alignment: .center)
            .cornerRadius(10)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(.black, lineWidth: 1)
            )
            .padding(20)
            .offset(x: offset.width, y: 0)
            .animation(isAnimating ? .spring(response: 0.4, dampingFraction: 0.6) : .none, value: offset)
        
            .onAppear { viewModel.startObserving() }
            .onTapGesture {
                viewModel.processAction(action: CardAction.Flip())
            }.gesture(
                DragGesture()
                    .onChanged { gesture in
                        // Update the card position as the user drags
                        offset = gesture.translation
                    }
                    .onEnded { value in
                        isAnimating = true
                        
                        // If dragged far enough to the right, trigger right swipe
                        if value.translation.width > 100 {
                            // Animate the card off screen to the right
                            offset = CGSize(width: UIScreen.main.bounds.width, height: 0)
                            viewModel.processAction(action: CardAction.SwipeRight())
                        }
                        // If dragged far enough to the left, trigger left swipe
                        else if value.translation.width < -100 {
                            // Animate the card off screen to the left
                            offset = CGSize(width: -UIScreen.main.bounds.width, height: 0)
                            viewModel.processAction(action: CardAction.SwipeLeft())
                        }
                        // If not dragged far enough, return to center
                        else {
                            offset = .zero
                        }
                        
                        // Reset animation flag and offset after animation completes
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) {
                            isAnimating = false
                            if offset.width != 0 {
                                offset = .zero
                            }
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
        // Animation is now handled in the view
        // Just need to reset the trigger after a delay to match animation timing
        Task {
            try? await Task.sleep(nanoseconds: 400_000_000) // 0.4 seconds
            _animationCompleteTrigger.value = false
        }
    }
    
    override func swipeRight() {
        _animationCompleteTrigger.value = true
        // Animation is now handled in the view
        // Just need to reset the trigger after a delay to match animation timing
        Task {
            try? await Task.sleep(nanoseconds: 400_000_000) // 0.4 seconds
            _animationCompleteTrigger.value = false
        }
    }
    
}

struct CardView: View {
    let currentCard: Card
    let isFlipped: Bool
    
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.white)
                .shadow(radius: 3)
            
            VStack {
                Text(currentCard.front)
                    .font(.title)
                    .padding()
                
                if isFlipped {
                    Divider()
                    Text(currentCard.back)
                        .font(.title)
                        .padding()
                }
            }
        }
    }
}
