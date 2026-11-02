export type Profile = {
  id: number
  name: string
  headline: string
  city: string
  story: string
  interests: string[]
  matchPercent: number
  sharedInterests: string[]
}

export type Account = {
  profile: Profile
  gender: 'MAN' | 'WOMAN'
  accountStatus: 'ACTIVE' | 'SUBSCRIPTION_REQUIRED' | 'PENDING_VERIFICATION'
  verificationStatus: 'PENDING' | 'APPROVED' | 'REJECTED'
  instagramHandle: string
  instagramVerified: boolean
  voiceRecordingSubmitted: boolean
  subscriptionActive: boolean
  creditsBalance: number
  phoneNumber: string
  accountId: string
  admin: boolean
}

export type Connection = {
  requestId: number
  status: 'PENDING' | 'ACCEPTED'
  direction: 'INCOMING' | 'OUTGOING' | 'CONNECTED'
  createdAt: string
  profile: Profile
}

export type ChatMessage = {
  id: number
  senderId: number
  body: string
  createdAt: string
}

export type InstagramShare = {
  senderId: number
  handle: string
  sharedAt: string
}

export type VerificationCase = {
  profileId: number
  name: string
  gender: 'MAN' | 'WOMAN'
  headline: string
  city: string
  story: string
  instagramHandle: string
  voiceRecordingSubmitted: boolean
}
