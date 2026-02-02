'use client';

import Link from 'next/link';
import { useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { usePathname } from 'next/navigation';
import { Film, User, LogOut, Menu, X } from 'lucide-react';

export default function Header() {
  const { user, isAuthenticated, logout } = useAuth();
  const pathname = usePathname();
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    // 쿠키 삭제가 완료되도록 약간의 지연 후 페이지 이동
    setTimeout(() => {
      window.location.href = '/';
    }, 100);
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-white shadow-sm">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center">
          {/* 좌측: 로고 + (데스크톱) 메뉴 */}
          <div className="flex items-center">
            <Link href="/" className="flex items-center gap-2">
              <Film className="h-6 w-6 text-blue-600" />
              <span className="text-xl font-bold text-gray-900">MovieSpot</span>
            </Link>

            {/* 메뉴: 로고 기준 60px 떨어진 위치에서 시작 (<=780px에서는 숨김) */}
            <nav className="hidden min-[781px]:flex items-center ml-[60px] gap-[20px]">
              <Link
                href="/"
                className={`min-w-[60px] text-center text-base font-medium transition-colors hover:text-blue-600 ${
                  pathname === '/' ? 'text-blue-600' : 'text-gray-700'
                }`}
              >
                홈
              </Link>
              <Link
                href="/movies"
                className={`min-w-[60px] text-center text-base font-medium transition-colors hover:text-blue-600 ${
                  pathname === '/movies' ? 'text-blue-600' : 'text-gray-700'
                }`}
              >
                영화 목록
              </Link>
              <Link
                href="/reviews"
                className={`min-w-[60px] text-center text-base font-medium transition-colors hover:text-blue-600 ${
                  pathname?.startsWith('/reviews') ? 'text-blue-600' : 'text-gray-700'
                }`}
              >
                리뷰
              </Link>
              <Link
                href="/collection"
                className={`min-w-[60px] text-center text-base font-medium transition-colors hover:text-blue-600 ${
                  pathname === '/collection' ? 'text-blue-600' : 'text-gray-700'
                }`}
              >
                나의 컬렉션
              </Link>
            </nav>
          </div>

          {/* 로그인/사용자 정보 */}
          <div className="ml-auto flex items-center gap-4">
            {isAuthenticated ? (
              <>
                <Link
                  href="/profile"
                  className="hidden min-[781px]:flex items-center gap-2 rounded-md px-2 py-1 text-sm text-gray-700 transition-colors hover:bg-gray-100 hover:text-blue-600 active:bg-gray-200"
                >
                  <User className="h-4 w-4" />
                  <span>{user?.nickname || user?.email || '사용자'}</span>
                </Link>
                <button
                  onClick={handleLogout}
                  className="flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-100"
                >
                  <LogOut className="h-4 w-4" />
                  <span className="hidden sm:inline">로그아웃</span>
                </button>
              </>
            ) : (
              <Link
                href="/login"
                className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-blue-700"
              >
                로그인
              </Link>
            )}

            {/* 모바일(<=780px): 햄버거 버튼 */}
            <button
              type="button"
              aria-label="메뉴 열기"
              onClick={() => setIsMenuOpen(true)}
              className="min-[781px]:hidden inline-flex items-center justify-center rounded-lg p-2 text-gray-700 hover:bg-gray-100"
            >
              <Menu className="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>

      {/* 모바일 드로어 */}
      {isMenuOpen && (
        <div className="min-[781px]:hidden fixed inset-0 z-[60]">
          {/* dim */}
          <button
            type="button"
            aria-label="메뉴 닫기"
            onClick={() => setIsMenuOpen(false)}
            className="absolute inset-0 bg-black/50"
          />

          {/* panel */}
          <aside className="absolute right-0 top-0 h-full w-[320px] max-w-[85vw] bg-white shadow-xl">
            <div className="flex items-center justify-between border-b border-gray-200 p-4">
              <span className="text-lg font-bold text-gray-900">메뉴</span>
              <button
                type="button"
                aria-label="닫기"
                onClick={() => setIsMenuOpen(false)}
                className="rounded-lg p-2 text-gray-700 hover:bg-gray-100"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            {/* 로그인/프로필 영역 */}
            <div className="border-b border-gray-200 p-4">
              {isAuthenticated ? (
                <div className="flex items-center justify-between gap-3">
                  <Link
                    href="/profile"
                    onClick={() => setIsMenuOpen(false)}
                    className="flex min-w-0 items-center gap-2 rounded-lg px-2 py-2 text-gray-900 hover:bg-gray-50"
                  >
                    <User className="h-5 w-5 text-gray-600" />
                    <span className="truncate font-medium">
                      {user?.nickname || user?.email || '사용자'}
                    </span>
                  </Link>
                  <button
                    type="button"
                    onClick={async () => {
                      setIsMenuOpen(false);
                      await handleLogout();
                    }}
                    className="whitespace-nowrap rounded-lg border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
                  >
                    로그아웃
                  </button>
                </div>
              ) : (
                <Link
                  href="/login"
                  onClick={() => setIsMenuOpen(false)}
                  className="block w-full rounded-lg bg-blue-600 px-4 py-3 text-center text-sm font-medium text-white hover:bg-blue-700"
                >
                  로그인하기
                </Link>
              )}
            </div>

            {/* 메뉴 리스트 */}
            <nav className="p-2">
              {[
                { href: '/', label: '홈', active: pathname === '/' },
                { href: '/movies', label: '영화 목록', active: pathname === '/movies' },
                { href: '/reviews', label: '리뷰', active: pathname?.startsWith('/reviews') },
                { href: '/collection', label: '나의 컬렉션', active: pathname === '/collection' },
              ].map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={() => setIsMenuOpen(false)}
                  className={`block rounded-lg px-4 py-3 text-base font-medium transition-colors ${
                    item.active ? 'bg-blue-50 text-blue-700' : 'text-gray-800 hover:bg-gray-50'
                  }`}
                >
                  {item.label}
                </Link>
              ))}
            </nav>
          </aside>
        </div>
      )}
    </header>
  );
}

