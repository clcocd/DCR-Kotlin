import {kotlinBridge} from "@/kotlin/bridge";

// TypeScript의 리터럴 키/값 타입은 유지하고 런타임 연산은 Kotlin/JS에 맡긴다.

/** 객체의 문자열 키 (심볼 제외, 숫자 키는 문자열로). */
type ObjectKeys<T extends object> = `${Exclude<keyof T, symbol>}`;

export const objectKeys = <T extends object>(value: T): ObjectKeys<T>[] =>
    kotlinBridge.typedObjectKeys(value) as ObjectKeys<T>[];

export const objectEntries = <T extends object>(value: T): [ObjectKeys<T>, T[Extract<keyof T, string | number>]][] =>
    kotlinBridge.typedObjectEntries(value) as [ObjectKeys<T>, T[Extract<keyof T, string | number>]][];

export const objectFromEntries = <K extends PropertyKey, V>(entries: Iterable<readonly [K, V]>): Partial<Record<K, V>> =>
    kotlinBridge.typedObjectFromEntries(entries) as Partial<Record<K, V>>;

export const arrayIncludes = <T extends U, U>(array: readonly T[], item: U): item is T =>
    kotlinBridge.typedArrayIncludes(array, item);
