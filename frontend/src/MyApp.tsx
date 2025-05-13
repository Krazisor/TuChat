import HomePage from "./pages/Homepage.tsx";
import {Route, Routes} from "react-router";
import Callback from "./pages/Callback.tsx";
import Dashboard from "./pages/dashboard/index.tsx";
import ProtectedRoute from "./components/auth/ProtectedRoute.tsx";

function MyApp() {

  return (
    <>
        <div style={{height: '100vh', width: '100%'}}>
            <Routes>
                <Route path="/" element={<HomePage/>}/>
                <Route path="/callback" element={<Callback />}/>
                <Route
                    path="/dashboard"
                    element={<ProtectedRoute element={<Dashboard/>}/>}
                />
                {/*<Route path="*" element={<Navigate to="/"/>}/>*/}
                {/*<Route path="/quizdesign" element={<MainDesign/>}/>*/}
                {/*<Route path="/doQuiz/:quizId" element={<DoQuizPage/>}/>*/}
                {/*<Route path="/quizComplete" element={<QuizCompletionPage/>}/>*/}
                {/*<Route path="/quizSquare" element={<QuizSquare/>}/>*/}

                {/*<Route path="/quiz-analysis" element={<ProtectedRoute element={<QuizAnalysisCenter/>}/>}/>*/}
            </Routes>
        </div>
    </>
  )
}

export default MyApp
