// 전역 alert, confirm 함수 제공
export const alert = (message, title = '', type = 'info') => {
  if (typeof window !== 'undefined' && window.$alert) {
    window.$alert(message, title, type)
  } else {
    // fallback to browser alert
    window.alert(message)
  }
}

export const confirm = (message, title = '') => {
  if (typeof window !== 'undefined' && window.$confirm) {
    return window.$confirm(message, title)
  } else {
    // fallback to browser confirm
    return Promise.resolve(window.confirm(message))
  }
}

export const useAlert = () => {
  return {
    alert,
    confirm
  }
}

