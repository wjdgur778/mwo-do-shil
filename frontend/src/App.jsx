import { useState, useEffect } from 'react'
import './App.css'

function App() {
  const [currentMessage, setCurrentMessage] = useState('greeting')
  const [isMessageVisible, setIsMessageVisible] = useState(true)
  const [showButton, setShowButton] = useState(false)
  const [showFireworks, setShowFireworks] = useState(false)
  
  // 친구 이름 - 여기서 수정하세요
  const friendName = "찬미"
  
  // 메시지 내용 - 여기서 수정하세요
  const messages = [
    { type: 'greeting', content: `안녕~ ${friendName} 💕` },
    { type: 'custom1', content: "오늘 하루도 힘내고 있지?" },
    { type: 'custom2', content: "피곤할텐데 고생이야 아가~" },
    { type: 'love', content: "사랑해! ❤️" }
  ]
  
  // 버튼 내용 - 여기서 수정하세요
  const buttonText = "클릭해봐! 🎉"

  useEffect(() => {
    // 3초 후에 첫 번째 메시지로 변경
    const message1Timer = setTimeout(() => {
      setIsMessageVisible(false)
      setTimeout(() => {
        setCurrentMessage('custom1')
        setIsMessageVisible(true)
      }, 500)
    }, 3000)

    // 5초 후에 두 번째 메시지로 변경
    const message2Timer = setTimeout(() => {
      setIsMessageVisible(false)
      setTimeout(() => {
        setCurrentMessage('custom2')
        setIsMessageVisible(true)
      }, 500)
    }, 6000)

    // 7초 후에 버튼 표시
    const buttonTimer = setTimeout(() => {
      setShowButton(true)
    }, 8000)

    return () => {
      clearTimeout(message1Timer)
      clearTimeout(message2Timer)
      clearTimeout(buttonTimer)
    }
  }, [])

  const handleButtonClick = () => {
    setIsMessageVisible(false)
    setShowButton(false)
    setTimeout(() => {
      setCurrentMessage('love')
      setShowFireworks(true)
      setIsMessageVisible(true)
    }, 500)
  }

  return (
    <div className="landing-page">
      {/* 폭죽 효과 */}
      {showFireworks && (
        <div className="fireworks">
          {[...Array(20)].map((_, i) => (
            <div key={i} className="firework" style={{ '--delay': `${i * 0.1}s` }}></div>
          ))}
        </div>
      )}
      
      {/* 중앙 메시지 컨테이너 */}
      <div className="message-container">
        {currentMessage && (
          <div className={`message-content ${isMessageVisible ? 'fade-in' : 'fade-out'}`}>
            <h2 className={
              currentMessage === 'greeting' ? 'greeting-text' :
              currentMessage === 'custom1' || currentMessage === 'custom2' ? 'custom-text' :
              currentMessage === 'love' ? 'love-text' : ''
            }>
              {messages.find(msg => msg.type === currentMessage)?.content}
            </h2>
          </div>
        )}
      </div>
      
      {/* 하단 버튼 */}
      {showButton && (
        <div className="button-container fade-in">
          <button onClick={handleButtonClick} className="love-button">
            {buttonText}
          </button>
        </div>
      )}
    </div>
  )
}

export default App
