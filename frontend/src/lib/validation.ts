export const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/

export const isEmail = (v: string) => EMAIL_RE.test(v.trim())
