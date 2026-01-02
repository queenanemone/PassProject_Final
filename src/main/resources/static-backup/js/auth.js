// 로그인/회원가입 탭 전환
document.getElementById('loginTab')?.addEventListener('click', () => {
    document.getElementById('loginForm').classList.remove('hidden');
    document.getElementById('registerForm').classList.add('hidden');
    document.getElementById('loginTab').classList.add('bg-primary', 'text-slate-900');
    document.getElementById('loginTab').classList.remove('text-text-secondary-dark');
    document.getElementById('registerTab').classList.remove('bg-primary', 'text-slate-900');
    document.getElementById('registerTab').classList.add('text-text-secondary-dark');
});

document.getElementById('registerTab')?.addEventListener('click', () => {
    document.getElementById('loginForm').classList.add('hidden');
    document.getElementById('registerForm').classList.remove('hidden');
    document.getElementById('registerTab').classList.add('bg-primary', 'text-slate-900');
    document.getElementById('registerTab').classList.remove('text-text-secondary-dark');
    document.getElementById('loginTab').classList.remove('bg-primary', 'text-slate-900');
    document.getElementById('loginTab').classList.add('text-text-secondary-dark');
});

// 로그인 폼 제출
document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });
        
        let result;
        try {
            result = await response.json();
        } catch (parseError) {
            const text = await response.text();
            throw new Error(`응답 파싱 실패: ${text}`);
        }
        
        if (!response.ok) {
            // HTTP 오류 상태 코드인 경우
            const errorMessage = result?.message || `HTTP error! status: ${response.status}`;
            alert(errorMessage);
            return;
        }
        
        if (result && result.success && result.data) {
            if (!result.data.token || !result.data.user) {
                alert('서버 응답 형식이 올바르지 않습니다');
                console.error('응답 데이터:', result);
                return;
            }
            localStorage.setItem('token', result.data.token);
            localStorage.setItem('user', JSON.stringify(result.data.user));
            window.location.href = 'dashboard.html';
        } else {
            alert(result?.message || '로그인에 실패했습니다');
        }
    } catch (error) {
        console.error('로그인 오류:', error);
        alert('로그인 중 오류가 발생했습니다: ' + (error.message || '알 수 없는 오류'));
    }
});

// 회원가입 폼 제출
document.getElementById('registerForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const name = document.getElementById('name').value;
    
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password, name })
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP error! status: ${response.status}, body: ${errorText}`);
        }
        
        const result = await response.json();
        
        if (result.success) {
            localStorage.setItem('token', result.data.token);
            localStorage.setItem('user', JSON.stringify(result.data.user));
            window.location.href = 'dashboard.html';
        } else {
            alert(result.message || '회원가입에 실패했습니다');
        }
    } catch (error) {
        console.error('회원가입 오류:', error);
        alert('회원가입 중 오류가 발생했습니다: ' + (error.message || '알 수 없는 오류'));
    }
});

// Google 로그인 초기화
let googleClientId = null;

// Google Identity Services 스크립트 로드 확인
function waitForGoogleScript(callback, maxAttempts = 50) {
    let attempts = 0;
    const checkInterval = setInterval(() => {
        attempts++;
        if (typeof google !== 'undefined' && google.accounts && google.accounts.oauth2) {
            clearInterval(checkInterval);
            console.log('Google Identity Services 로드 완료');
            callback();
        } else if (attempts >= maxAttempts) {
            clearInterval(checkInterval);
            console.error('Google Identity Services 로드 시간 초과');
            console.log('google 객체:', typeof google);
            if (typeof google !== 'undefined') {
                console.log('google.accounts:', typeof google.accounts);
                console.log('google.accounts.oauth2:', typeof google.accounts?.oauth2);
            }
        }
    }, 100);
}

// Google Identity Services 초기화
function initializeGoogleSignIn() {
    console.log('Google 로그인 초기화 시작');
    
    // Google Client ID는 백엔드에서 가져오거나 환경 변수로 설정
    fetch('/api/auth/google/client-id')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                googleClientId = data.data;
                console.log('Google Client ID 로드 성공:', googleClientId);
                
                // Google 스크립트가 로드될 때까지 대기
                waitForGoogleScript(() => {
                    setupGoogleSignIn();
                });
            } else {
                console.error('Google Client ID를 가져올 수 없습니다:', data);
            }
        })
        .catch(error => {
            console.error('Google Client ID 로드 오류:', error);
        });
}

function setupGoogleSignIn() {
    console.log('Google 로그인 설정 시작');
    console.log('googleClientId:', googleClientId);
    
    if (typeof google === 'undefined' || !google.accounts) {
        console.error('Google Identity Services가 로드되지 않았습니다');
        return;
    }

    if (!googleClientId || googleClientId === 'YOUR_GOOGLE_CLIENT_ID_HERE') {
        console.warn('Google Client ID가 설정되지 않았습니다. application.yml에서 설정해주세요.');
        alert('Google Client ID가 설정되지 않았습니다. 관리자에게 문의하세요.');
        return;
    }

    // Google 로그인 버튼 클릭 이벤트
    const googleButton = document.getElementById('google-signin-button');
    if (!googleButton) {
        console.error('Google 로그인 버튼을 찾을 수 없습니다');
        return;
    }
    
    console.log('Google 로그인 버튼 찾음, 이벤트 리스너 추가');
    
    // 클릭 이벤트 핸들러
    const handleGoogleButtonClick = (e) => {
        e.preventDefault();
        e.stopPropagation();
        console.log('Google 로그인 버튼 클릭됨');
        
        if (typeof google === 'undefined' || !google.accounts || !google.accounts.oauth2) {
            alert('Google 로그인 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
            return;
        }
        
        try {
            // Google OAuth 2.0 토큰 클라이언트 초기화 및 요청
            const tokenClient = google.accounts.oauth2.initTokenClient({
                client_id: googleClientId,
                scope: 'email profile',
                callback: async (tokenResponse) => {
                    if (tokenResponse.error) {
                        console.error('Google 로그인 오류:', tokenResponse.error);
                        alert('Google 로그인에 실패했습니다: ' + tokenResponse.error);
                        return;
                    }
                    
                    console.log('Google 토큰 받음, 사용자 정보 가져오는 중...');
                    
                    // 액세스 토큰으로 사용자 정보 가져오기
                    try {
                        const userInfoResponse = await fetch(`https://www.googleapis.com/oauth2/v2/userinfo?access_token=${tokenResponse.access_token}`);
                        if (!userInfoResponse.ok) {
                            throw new Error(`HTTP error! status: ${userInfoResponse.status}`);
                        }
                        const userInfo = await userInfoResponse.json();
                        console.log('사용자 정보:', userInfo);
                        await handleGoogleUserInfo(userInfo);
                    } catch (error) {
                        console.error('사용자 정보 가져오기 오류:', error);
                        alert('사용자 정보를 가져오는 중 오류가 발생했습니다: ' + error.message);
                    }
                }
            });
            
            // 토큰 요청
            tokenClient.requestAccessToken({ prompt: 'consent' });
        } catch (error) {
            console.error('Google 로그인 초기화 오류:', error);
            alert('Google 로그인 초기화 중 오류가 발생했습니다: ' + error.message);
        }
    };
    
    // 기존 이벤트 리스너 제거 후 새로 추가
    googleButton.removeEventListener('click', handleGoogleButtonClick);
    googleButton.addEventListener('click', handleGoogleButtonClick);
    
    console.log('Google 로그인 설정 완료');
}

// Google 로그인 성공 콜백 (credential 방식)
async function handleGoogleSignIn(response) {
    try {
        // Google에서 받은 credential을 백엔드로 전송
        const backendResponse = await fetch('/api/auth/google', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ credential: response.credential })
        });

        const result = await backendResponse.json();

        if (backendResponse.ok && result.success) {
            console.log('Google 로그인 성공 (credential 방식) - 사용자 정보:', result.data.user);
            console.log('프로필 이미지:', result.data.user?.profileImage);
            localStorage.setItem('token', result.data.token);
            localStorage.setItem('user', JSON.stringify(result.data.user));
            window.location.href = 'dashboard.html';
        } else {
            alert(result.message || 'Google 로그인에 실패했습니다');
        }
    } catch (error) {
        console.error('Google 로그인 오류:', error);
        alert('Google 로그인 중 오류가 발생했습니다: ' + (error.message || '알 수 없는 오류'));
    }
}

// Google 사용자 정보로 로그인 처리 (OAuth 2.0 토큰 방식)
async function handleGoogleUserInfo(userInfo) {
    try {
        // 사용자 정보를 직접 전송 (credential 없이)
        // 백엔드에서 email, name, picture를 직접 처리
        const backendResponse = await fetch('/api/auth/google', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ 
                email: userInfo.email,
                name: userInfo.name,
                picture: userInfo.picture
            })
        });

        const result = await backendResponse.json();

        if (backendResponse.ok && result.success) {
            console.log('Google 로그인 성공 (OAuth 2.0 방식) - 사용자 정보:', result.data.user);
            console.log('프로필 이미지:', result.data.user?.profileImage);
            localStorage.setItem('token', result.data.token);
            localStorage.setItem('user', JSON.stringify(result.data.user));
            window.location.href = 'dashboard.html';
        } else {
            alert(result.message || 'Google 로그인에 실패했습니다');
        }
    } catch (error) {
        console.error('Google 로그인 오류:', error);
        alert('Google 로그인 중 오류가 발생했습니다: ' + (error.message || '알 수 없는 오류'));
    }
}

// Google 로그인 버튼 클릭 핸들러 (즉시 연결)
function attachGoogleButtonHandler() {
    const googleButton = document.getElementById('google-signin-button');
    if (googleButton) {
        googleButton.addEventListener('click', async (e) => {
            e.preventDefault();
            e.stopPropagation();
            console.log('Google 로그인 버튼 클릭됨 (즉시 핸들러)');
            
            // Client ID가 없으면 먼저 가져오기
            if (!googleClientId) {
                try {
                    const response = await fetch('/api/auth/google/client-id');
                    const data = await response.json();
                    if (data.success && data.data) {
                        googleClientId = data.data;
                    } else {
                        alert('Google Client ID를 가져올 수 없습니다.');
                        return;
                    }
                } catch (error) {
                    console.error('Client ID 가져오기 오류:', error);
                    alert('Google 로그인 설정을 불러오는 중 오류가 발생했습니다.');
                    return;
                }
            }
            
            // Google 스크립트가 로드될 때까지 대기
            if (typeof google === 'undefined' || !google.accounts || !google.accounts.oauth2) {
                alert('Google 로그인 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
                console.log('Google 스크립트 로드 대기 중...');
                
                // 2초 후 다시 시도
                setTimeout(() => {
                    if (typeof google !== 'undefined' && google.accounts && google.accounts.oauth2) {
                        triggerGoogleLogin();
                    } else {
                        alert('Google 로그인 서비스를 불러올 수 없습니다. 페이지를 새로고침해주세요.');
                    }
                }, 2000);
                return;
            }
            
            triggerGoogleLogin();
        });
        console.log('Google 로그인 버튼 핸들러 연결 완료');
    }
}

// Google 로그인 트리거
function triggerGoogleLogin() {
    try {
        const tokenClient = google.accounts.oauth2.initTokenClient({
            client_id: googleClientId,
            scope: 'email profile',
            callback: async (tokenResponse) => {
                if (tokenResponse.error) {
                    console.error('Google 로그인 오류:', tokenResponse.error);
                    alert('Google 로그인에 실패했습니다: ' + tokenResponse.error);
                    return;
                }
                
                console.log('Google 토큰 받음, 사용자 정보 가져오는 중...');
                
                try {
                    const userInfoResponse = await fetch(`https://www.googleapis.com/oauth2/v2/userinfo?access_token=${tokenResponse.access_token}`);
                    if (!userInfoResponse.ok) {
                        throw new Error(`HTTP error! status: ${userInfoResponse.status}`);
                    }
                    const userInfo = await userInfoResponse.json();
                    console.log('사용자 정보:', userInfo);
                    await handleGoogleUserInfo(userInfo);
                } catch (error) {
                    console.error('사용자 정보 가져오기 오류:', error);
                    alert('사용자 정보를 가져오는 중 오류가 발생했습니다: ' + error.message);
                }
            }
        });
        
        tokenClient.requestAccessToken({ prompt: 'consent' });
    } catch (error) {
        console.error('Google 로그인 초기화 오류:', error);
        alert('Google 로그인 초기화 중 오류가 발생했습니다: ' + error.message);
    }
}

// 페이지 로드 시 Google 로그인 초기화
function startGoogleLoginInit() {
    // 즉시 버튼 핸들러 연결
    attachGoogleButtonHandler();
    
    // DOM이 완전히 로드된 후 초기화
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            setTimeout(initializeGoogleSignIn, 500);
        });
    } else {
        setTimeout(initializeGoogleSignIn, 500);
    }
}

startGoogleLoginInit();

