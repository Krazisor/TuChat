import React from 'react';

interface FullScreenLoadingProps {
    type?: 'spinner' | 'dots' | 'pulse' | 'bar';
    size?: 'small' | 'medium' | 'large';
    color?: string;
    text?: string;
    backgroundColor?: string;
}

const SIZE_MAP = {
    small: 24,
    medium: 40,
    large: 64,
};

const injectStyles = () => {
    if (document.getElementById('fs-loader-style')) return;
    const style = document.createElement('style');
    style.id = 'fs-loader-style';
    style.innerHTML = `
.fullscreen-loading {
  position: fixed;
  top: 0; left: 0; right: 0; bottom:0;
  width: 100vw; height: 100vh;
  display: flex; align-items: center; justify-content: center;
  z-index: 9999;
  background-color: rgba(255,255,255,0.85);
  transition: background .2s;
}
.loading-wrapper {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 20px; text-align: center;
}
.loading-text {
  margin-top: 18px; font-size: 17px; font-weight: 500;
  letter-spacing: 0.02em;
}
/* ------ Spinner (带渐变轨道) ------- */
.loading-spinner {
  border-radius: 50%;
  border: 4px solid transparent;
  border-top: 4px solid #0af;
  border-right: 4px solid #0af9;
  border-bottom: 4px solid #0af6;
  border-left: 4px solid #0af2;
  animation: spin 0.9s infinite linear;
  background: conic-gradient(rgba(0,0,0,0.07), #dcefff, transparent 150deg);
}
.loading-spinner.small { width: 24px; height: 24px; border-width: 3px; }
.loading-spinner.medium { width: 40px; height: 40px; }
.loading-spinner.large { width: 64px; height: 64px; border-width: 5px; }

@keyframes spin {
  0% { transform: rotate(0deg);}
  100% { transform: rotate(360deg);}
}

/* -------- 彩色跳动点 Dots --------- */
.loading-dots {
  display: flex; align-items: center; justify-content: center; gap: 8px;
}
.loading-dots div {
  width: 12px; height: 12px;
  border-radius: 50%;
  animation: dots-bounce 1.2s infinite both;
  background: linear-gradient(135deg, #90f 40%, #0af 60%);
  box-shadow: 0 0 8px 2px #0af4;
  opacity: .7;
}
.loading-dots.small div { width: 7px; height: 7px;}
.loading-dots.medium div { width: 12px; height: 12px;}
.loading-dots.large div { width: 18px; height: 18px;}
.loading-dots div:nth-child(1) { animation-delay: -0.32s;}
.loading-dots div:nth-child(2) { animation-delay: -0.16s;}
@keyframes dots-bounce {
  0%, 80%, 100% { transform: scale(0.7);}
  40% { transform: scale(1.3);}
}

/* -------- Pulse 脉冲波放大 + 发光 -------- */
.loading-pulse {
  border-radius: 50%;
  background-color: #3498db;
  animation: pulse 1.1s infinite cubic-bezier(0.215, 0.61, 0.355, 1);
  box-shadow: 0 0 0 0 #56f7, 0 0 6px 3px #0af4;
}
.loading-pulse.small { width: 24px; height: 24px; }
.loading-pulse.medium { width: 40px; height: 40px; }
.loading-pulse.large { width: 56px; height: 56px; }
@keyframes pulse {
  0% { transform: scale(.6); opacity: 1; box-shadow: 0 0 0 0 #56f7;}
  70% { transform: scale(1); opacity: .7; box-shadow: 0 0 12px 8px #0af3;}
  100% { transform: scale(.6); opacity: .3; box-shadow: 0 0 0 0 #56f2;}
}

/* -------- 新增 bar 进度条类型 -------- */
.loading-bar {
  width: 120px; height: 6px;
  background: #e5eeff;
  border-radius: 3px;
  overflow: hidden;
  position: relative;
  margin: 8px 0 0 0;
}
.loading-bar .bar {
  position: absolute; top: 0; bottom: 0; left: -40%; width: 40%;
  background: linear-gradient(90deg, #90f, #0af, #2ecc40);
  box-shadow: 0 0 8px #0af7;
  border-radius: 3px;
  animation: move-bar 1.3s infinite linear;
}
.loading-bar.small { width: 72px; height: 4px;}
.loading-bar.medium { width: 120px; height: 6px;}
.loading-bar.large { width: 160px; height: 8px;}

@keyframes move-bar {
  0%   { left: -40%;}
  60%  { left: 100%;}
  100% { left: 100%;}
}
`;
    document.head.appendChild(style);
};

const FullScreenLoading: React.FC<FullScreenLoadingProps> = ({
                                                                 type = 'spinner',
                                                                 size = 'large',
                                                                 color = '#3498db',
                                                                 text = 'Loading...',
                                                                 backgroundColor = 'rgba(255,255,255,0.89)'
                                                             }) => {
    // 挂载到页面时插入样式
    React.useEffect(injectStyles, []);

    // 动态颜色覆盖
    const dynamicStyle: React.CSSProperties = {};
    switch (type) {
        case 'spinner':
            dynamicStyle.borderTopColor = color;
            dynamicStyle.borderRightColor = color + '99';
            dynamicStyle.borderBottomColor = color + '66';
            dynamicStyle.borderLeftColor = color + '22';
            break;
        case 'dots':
            dynamicStyle.color = color;
            break;
        case 'pulse':
            dynamicStyle.backgroundColor = color;
            break;
        case 'bar':
            // handled inline-bar's gradient below
            break;
    }

    // loader 渲染
    const renderLoader = () => {
        switch (type) {
            case 'dots':
                return (
                    <div className={`loading-dots ${size}`} style={{color}}>
                        <div/>
                        <div/>
                        <div/>
                    </div>
                );
            case 'pulse':
                return (
                    <div className={`loading-pulse ${size}`} style={dynamicStyle}></div>
                );
            case 'bar':
                return (
                    <div className={`loading-bar ${size}`}>
                        <div className="bar"
                             style={{
                                 background: `linear-gradient(90deg, ${color}, #0af, #2ecc40)`
                             }}/>
                    </div>
                );
            case 'spinner':
            default:
                return (
                    <div className={`loading-spinner ${size}`} style={dynamicStyle}></div>
                );
        }
    };

    return (
        <div className="fullscreen-loading" style={{backgroundColor}}>
            <div className="loading-wrapper">
                {renderLoader()}
                {text && (
                    <div className="loading-text" style={{color}}>
                        {text}
                    </div>
                )}
            </div>
        </div>
    );
};

export default FullScreenLoading;