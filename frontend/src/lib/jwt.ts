/**
 * JWT utility functions for decoding and extracting user information
 */

export interface JwtPayload {
  sub: string // email
  userId: number
  username: string
  firstName: string
  lastName: string
  roles: string[]
  permissions: string[]
  isAdmin: boolean
  isSuperAdmin: boolean
  iat: number
  exp: number
}

/**
 * Decode JWT token without verification (client-side only)
 * Note: This is for extracting user info only, server still validates the token
 */
export function decodeJwt(token: string): JwtPayload | null {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) {
      return null
    }

    const payload = parts[1]
    const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
    return decoded as JwtPayload
  } catch (error) {
    console.error('Failed to decode JWT:', error)
    return null
  }
}

/**
 * Check if JWT token is expired
 */
export function isTokenExpired(token: string): boolean {
  const payload = decodeJwt(token)
  if (!payload) return true
  
  const currentTime = Math.floor(Date.now() / 1000)
  return payload.exp < currentTime
}

/**
 * Get token expiration time
 */
export function getTokenExpiration(token: string): Date | null {
  const payload = decodeJwt(token)
  if (!payload) return null
  
  return new Date(payload.exp * 1000)
}