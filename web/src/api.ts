import type { Account, AdminAnalytics, AdminUser, ChatMessage, Connection, CreditPurchase, CreditPurchaseReviewResult, InstagramShare, Profile, SafetyReport, SafetyReportCategory, VerificationCase } from './types'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers)
  if (!(init?.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }
  const response = await fetch(path, {
    ...init,
    credentials: 'include',
    headers,
  })

  if (!response.ok) {
    const body = await response.json().catch(() => null) as {
      detail?: string
      message?: string
      error?: string
      title?: string
    } | null
    throw new Error(body?.detail ?? body?.message ?? body?.error ?? body?.title ?? `Request failed (${response.status})`)
  }

  const body = await response.text()
  return (body ? JSON.parse(body) : undefined) as T
}

export const getCurrentProfile = () => request<Account>('/api/profiles/me')
export const getDiscoverProfiles = (search: string) =>
  request<Profile[]>(`/api/profiles/discover?search=${encodeURIComponent(search)}`)
export const getConnections = () => request<Connection[]>('/api/connections')
export const requestConnection = (profileId: number, note: string) =>
  request<Connection>(`/api/connections/${profileId}`, {
    method: 'POST',
    body: JSON.stringify({ note }),
  })
export const sendEmailCode = (email: string) =>
  request<{ message: string; demoCode: string }>('/api/auth/email/send-code', {
    method: 'POST',
    body: JSON.stringify({ email }),
  })
export const verifyEmailCode = (email: string, code: string) =>
  request<{ message: string; verificationToken: string }>('/api/auth/email/verify', {
    method: 'POST',
    body: JSON.stringify({ email, code }),
  })
export const sendAccountIdRecoveryCode = (email: string) =>
  request<{ message: string; demoCode: string }>('/api/auth/account-id/recovery/send-code', {
    method: 'POST',
    body: JSON.stringify({ email }),
  })
export const recoverAccountId = (email: string, code: string) =>
  request<{ message: string; accountId: string | null }>('/api/auth/account-id/recovery', {
    method: 'POST',
    body: JSON.stringify({ email, code }),
  })
export const acceptConnection = (requestId: number) =>
  request<Connection>(`/api/connections/${requestId}/accept`, { method: 'PATCH' })
export const startSignup = (details: {
  emailVerificationToken: string
  name: string
  phoneNumber: string
  dateOfBirth: string
  gender: string
  relationshipStatus: string
  city: string
  email: string
  purposes: string[]
}) =>
  request<{ signupToken: string; accountId: string }>('/api/auth/signup/start', {
    method: 'POST',
    body: JSON.stringify(details),
  })
export const sendPhoneCode = (signup: {
  signupToken: string
  password: string
  occupation: string
  nativeCity: string
  languages: string[]
  interests: string[]
  interestNotes: Record<string, string>
  valuesNote: string
  story: string
  instagramUrl: string
}) =>
  request<{ message: string; demoCode: string }>('/api/auth/send-code', {
    method: 'POST',
    body: JSON.stringify(signup),
  })
export const signIn = (identifier: string, password: string) =>
  request<Account>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ identifier, password }),
  })
export const adminSignIn = (email: string, password: string) =>
  request<Account>('/api/admin/auth/login', {
    method: 'POST',
    body: JSON.stringify({ identifier: email, password }),
  })
export const verifyPhoneCode = (phoneNumber: string, code: string) =>
  request<Account>('/api/auth/verify', {
    method: 'POST',
    body: JSON.stringify({ phoneNumber, code }),
  })
export const logout = () => request<void>('/api/auth/logout', { method: 'POST' })
export const getMessages = (requestId: number) =>
  request<ChatMessage[]>(`/api/connections/${requestId}/messages`)
export const sendMessage = (requestId: number, body: string) =>
  request<ChatMessage>(`/api/connections/${requestId}/messages`, {
    method: 'POST',
    body: JSON.stringify({ body }),
  })
export const getInstagramShares = (requestId: number) =>
  request<InstagramShare[]>(`/api/connections/${requestId}/instagram-shares`)
export const shareInstagram = (requestId: number) =>
  request<InstagramShare[]>(`/api/connections/${requestId}/instagram-share`, {
    method: 'POST',
  })
export const hideInstagram = (requestId: number) =>
  request<InstagramShare[]>(`/api/connections/${requestId}/instagram-share`, { method: 'DELETE' })
export const getVerificationQueue = () => request<VerificationCase[]>('/api/admin/verification-queue')
export const getCreditPurchaseQueue = () => request<CreditPurchase[]>('/api/admin/payment-queue')
export const getAdminPayments = () => request<CreditPurchase[]>('/api/admin/payments')
export const getAdminUsers = () => request<AdminUser[]>('/api/admin/users')
export const setAdminUserSuspended = (profileId: number, suspended: boolean) =>
  request<AdminUser>(`/api/admin/users/${profileId}/suspension`, {
    method: 'PATCH',
    body: JSON.stringify({ suspended }),
  })
export const getAdminAnalytics = () => request<AdminAnalytics>('/api/admin/analytics')
export const getSafetyReports = () => request<SafetyReport[]>('/api/admin/reports')
export const reviewSafetyReport = (reportId: number, status: 'RESOLVED' | 'DISMISSED') =>
  request<SafetyReport>(`/api/admin/reports/${reportId}`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  })
export const submitSafetyReport = (profileId: number, category: SafetyReportCategory, details: string) =>
  request<SafetyReport>('/api/safety/reports', {
    method: 'POST',
    body: JSON.stringify({ profileId, category, details }),
  })
export const reviewCreditPurchase = (purchaseId: number, approved: boolean) =>
  request<CreditPurchaseReviewResult>(`/api/admin/payments/${purchaseId}`, {
    method: 'PATCH',
    body: JSON.stringify({ approved }),
  })
export const getMyCreditPurchases = () => request<CreditPurchase[]>('/api/payments/credit-requests')
export const requestCreditPurchase = (packageCode: string, customCredits: number | null, paymentReference: string) =>
  request<CreditPurchase>('/api/payments/credit-requests', {
    method: 'POST',
    body: JSON.stringify({ packageCode, customCredits, paymentReference }),
  })
export const reviewVerification = (profileId: number, approved: boolean) =>
  request<void>(`/api/admin/profiles/${profileId}/verification`, {
    method: 'PATCH',
    body: JSON.stringify({ approved }),
  })
export const uploadVoiceRecording = (recording: File) => {
  const data = new FormData()
  data.append('recording', recording)
  return request<void>('/api/profiles/me/voice-verification', { method: 'POST', body: data })
}
export const getVoiceRecordingUrl = async (profileId: number) => {
  const response = await fetch(`/api/admin/profiles/${profileId}/voice`, { credentials: 'include' })
  if (!response.ok) throw new Error(`Could not load voice recording (${response.status})`)
  return URL.createObjectURL(await response.blob())
}
