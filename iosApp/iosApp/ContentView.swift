import SwiftUI
import Shared

struct ContentView: View {
    @ObservedObject private(set) var viewModel = IosCardViewModel()

    @State private var offset: CGSize = .zero
    @State private var isAnimating = false

    var body: some View {
        CardView(
            currentCard: viewModel.sCurrentCard,
            isFlipped: viewModel.sIsFlipped,
            isEditing: viewModel.sIsEditing,
            frontText: viewModel.frontText,
            backText: viewModel.backText,
            onEditTap: {
                viewModel.processAction(action: CardAction.Edit())
            },
            onSaveTap: {
                Task {
                    await viewModel.saveEdit()
                }
            },
            onCancelTap: {
                viewModel.processAction(action: CardAction.CancelEdit())
            },
            onFrontTextChanged: { newText in
                viewModel.frontText = newText
            },
            onBackTextChanged: { newText in
                viewModel.backText = newText
            }
        )
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
        // Only enable tap gesture for flip if not in editing mode
        .onTapGesture {
            if !viewModel.sIsEditing {
                viewModel.processAction(action: CardAction.Flip())
            }
        }
        // Only enable drag gesture if not in editing mode
        .gesture(
            DragGesture()
                .onChanged { gesture in
                    // Only update position if not in editing mode
                    if !viewModel.sIsEditing {
                        offset = gesture.translation
                    }
                }
                .onEnded { value in
                    // Only process swipe if not in editing mode
                    if !viewModel.sIsEditing {
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
                }
        )
    }
}

extension ContentView {
    @MainActor
    class IosCardViewModel: CommonCardViewModel, ObservableObject {
        @Published var sCurrentCard: Card = Card(front: "", back: "", learned: false)
        @Published var sIsFlipped: Bool = false
        @Published var sIsEditing: Bool = false
        @Published var frontText: String = ""
        @Published var backText: String = ""

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
                for await value in isEditing {
                    await MainActor.run {
                        self.sIsEditing = value.boolValue

                        // Initialize the edit text when entering edit mode
                        if self.sIsEditing {
                            self.frontText = self.sCurrentCard.front
                            self.backText = self.sCurrentCard.back
                        }
                    }
                }
            }
            Task {
                for await value in editCardContent {
                    await MainActor.run {
                        if let first = value.first as? String {
                            self.frontText = first
                        }
                        if let second = value.second as? String {
                            self.backText = second
                        }
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

        func updateEditContent() {
            updateEditContent(front: frontText, back: backText)
        }

        @MainActor
        func saveEdit() async {
            updateEditContent()
            do {
                try await saveCardEdit()
                processAction(action: CardAction.SaveEdit())
            } catch {
                print("Error saving card edit: \(error)")
            }
        }
    }
}

class IosCardAnimationManager: CommonCardAnimationManager {
    
    override func reset() {
        _animationCompleteTrigger.value = AnimationCompleteOutcome(idx: -1, learned: false)
    }
    
    override func swipeLeft(idx: Int32) {
        _animationCompleteTrigger.value = AnimationCompleteOutcome(idx: -1, learned: false)
        // Animation is now handled in the view
        // Just need to reset the trigger after a delay to match animation timing
        Task {
            try? await Task.sleep(nanoseconds: 400_000_000) // 0.4 seconds
            _animationCompleteTrigger.value = AnimationCompleteOutcome(idx: idx, learned: false)
        }
    }
    
    override func swipeRight(idx: Int32) {
        _animationCompleteTrigger.value = AnimationCompleteOutcome(idx: -1, learned: false)
        // Animation is now handled in the view
        // Just need to reset the trigger after a delay to match animation timing
        Task {
            try? await Task.sleep(nanoseconds: 400_000_000) // 0.4 seconds
            _animationCompleteTrigger.value = AnimationCompleteOutcome(idx: idx, learned: true)
        }
    }
    
}

struct CardView: View {
    let currentCard: Card
    let isFlipped: Bool
    let isEditing: Bool
    let frontText: String
    let backText: String
    var onEditTap: () -> Void
    var onSaveTap: () -> Void
    var onCancelTap: () -> Void
    var onFrontTextChanged: (String) -> Void
    var onBackTextChanged: (String) -> Void

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.white)
                .shadow(radius: 3)

            if isEditing {
                // Edit mode UI
                VStack {
                    // Front text field
                    VStack(alignment: .leading) {
                        Text("Front")
                            .font(.caption)
                            .foregroundColor(.gray)
                        TextEditor(text: Binding(
                            get: { frontText },
                            set: { onFrontTextChanged($0) }
                        ))
                        .font(.body)
                        .padding(8)
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(8)
                    }
                    .padding(.horizontal)

                    Divider()
                        .padding(.horizontal)

                    // Back text field
                    VStack(alignment: .leading) {
                        Text("Back")
                            .font(.caption)
                            .foregroundColor(.gray)
                        TextEditor(text: Binding(
                            get: { backText },
                            set: { onBackTextChanged($0) }
                        ))
                        .font(.body)
                        .padding(8)
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(8)
                    }
                    .padding(.horizontal)

                    // Buttons
                    HStack {
                        Spacer()
                        Button("Cancel") {
                            onCancelTap()
                        }
                        .padding(.horizontal)

                        Button("Save") {
                            onSaveTap()
                        }
                        .padding(.horizontal)
                    }
                    .padding()
                }
                .padding(.vertical)
            } else {
                // Normal view mode UI
                VStack {
                    HStack {
                        Text(currentCard.front)
                            .font(.title)
                            .padding()
                            .frame(maxWidth: .infinity, alignment: .leading)

                        Button(action: {
                            onEditTap()
                        }) {
                            Image(systemName: "pencil")
                                .font(.title2)
                                .foregroundColor(.blue)
                        }
                        .padding(.trailing)
                    }

                    if isFlipped {
                        Divider()
                            .padding(.horizontal)
                        Text(currentCard.back)
                            .font(.title)
                            .padding()
                    }

                    Spacer()
                }
            }
        }
    }
}
